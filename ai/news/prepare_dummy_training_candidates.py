#!/usr/bin/env python3
"""Build reviewable candidate examples for missing dummy-news labels."""

from __future__ import annotations

import argparse
from collections import Counter
from pathlib import Path
from typing import Any

from news_classifier_utils import (
    LABEL_ORDER,
    compose_embedding_text,
    load_articles_from_corpus,
    load_jsonl_rows,
    normalize_text,
    resolve_path,
    write_jsonl,
)


HEURISTIC_PROFILES = {
    "CRISIS_TO_BOOM": {
        "current": ["침체", "위기", "급감", "부진", "적자", "셧다운", "실업", "구조조정"],
        "future": ["투자유치", "수주", "반등", "급증", "호황", "흑자전환", "성장", "활황"],
    },
    "RECOVERY_TO_BOOM": {
        "current": ["회복", "개선", "반등", "정상화", "회복세", "회복 조짐"],
        "future": ["급증", "호황", "활황", "과열", "급등", "투자 확대", "사상 최대"],
    },
    "RECOVERY_TO_RECOVERY": {
        "current": ["회복", "개선", "반등", "정상화", "회복세", "완화"],
        "future": ["안정", "유지", "지속", "보합", "점진", "완만", "정상화"],
    },
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Prepare reviewable candidates for missing dummy-news labels."
    )
    parser.add_argument("--dummy-input", default="outputs/dummy_news.jsonl")
    parser.add_argument("--classified-input", default="outputs/classified.jsonl")
    parser.add_argument("--review-input", default="outputs/review.jsonl")
    parser.add_argument("--raw-input", default="경제_뉴스.json")
    parser.add_argument("--output", default="outputs/dummy_training_candidates.jsonl")
    parser.add_argument("--min-per-label", type=int, default=3)
    parser.add_argument("--max-existing-per-label", type=int, default=6)
    parser.add_argument("--max-keyword-per-label", type=int, default=10)
    return parser.parse_args()


def count_labels(rows: list[dict[str, Any]]) -> Counter[str]:
    counts: Counter[str] = Counter()
    for row in rows:
        label = row.get("final_label")
        if label:
            counts[str(label)] += 1
    return counts


def dedupe_by_doc_id(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    seen: set[str] = set()
    deduped: list[dict[str, Any]] = []
    for row in rows:
        doc_id = str(row.get("doc_id", ""))
        if not doc_id or doc_id in seen:
            continue
        seen.add(doc_id)
        deduped.append(row)
    return deduped


def rank_existing_candidates(rows: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return sorted(
        dedupe_by_doc_id(rows),
        key=lambda row: (
            bool(row.get("should_review", True)),
            -float(row.get("confidence", 0.0)),
            str(row.get("doc_id", "")),
        ),
    )


def score_keyword_candidate(row: dict[str, Any], target_label: str) -> int:
    profile = HEURISTIC_PROFILES[target_label]
    haystack = compose_embedding_text(row)
    current_hits = sum(1 for keyword in profile["current"] if keyword in haystack)
    future_hits = sum(1 for keyword in profile["future"] if keyword in haystack)
    return (current_hits * 3) + (future_hits * 4)


def mine_keyword_candidates(
    raw_articles: dict[str, dict[str, Any]],
    *,
    target_label: str,
    exclude_doc_ids: set[str],
    limit: int,
) -> list[dict[str, Any]]:
    scored: list[tuple[int, dict[str, Any]]] = []
    for article in raw_articles.values():
        doc_id = article["doc_id"]
        if doc_id in exclude_doc_ids:
            continue
        score = score_keyword_candidate(article, target_label)
        if score <= 0:
            continue
        scored.append((score, article))
    scored.sort(key=lambda item: (-item[0], item[1]["doc_id"]))

    candidates: list[dict[str, Any]] = []
    for score, article in scored[:limit]:
        candidates.append(
            {
                "candidate_label": target_label,
                "candidate_source": "keyword_search",
                "candidate_score": score,
                **article,
            }
        )
    return candidates


def attach_existing_metadata(row: dict[str, Any], target_label: str, source_name: str) -> dict[str, Any]:
    article = {
        "doc_id": row.get("doc_id"),
        "doc_title": normalize_text(str(row.get("doc_title", ""))),
        "doc_source": normalize_text(str(row.get("doc_source", ""))),
        "doc_published": int(row.get("doc_published", 0) or 0),
        "article_text": normalize_text(str(row.get("article_text", ""))),
    }
    return {
        "candidate_label": target_label,
        "candidate_source": source_name,
        "candidate_score": float(row.get("confidence", 0.0)),
        "source_label": row.get("final_label"),
        "source_confidence": row.get("confidence"),
        "source_should_review": row.get("should_review"),
        "source_reason": row.get("reason"),
        **article,
    }


def main() -> int:
    args = parse_args()
    dummy_rows = load_jsonl_rows(resolve_path(args.dummy_input))
    classified_rows = load_jsonl_rows(resolve_path(args.classified_input))
    review_rows = load_jsonl_rows(resolve_path(args.review_input))
    raw_articles = load_articles_from_corpus(resolve_path(args.raw_input))

    label_counts = count_labels(dummy_rows)
    needed_labels = [
        label
        for label in LABEL_ORDER
        if label_counts.get(label, 0) < args.min_per_label
    ]
    if not needed_labels:
        print("dummy dataset already satisfies the minimum per-label count.")
        write_jsonl(resolve_path(args.output), [])
        return 0

    base_doc_ids = {str(row.get("doc_id")) for row in dummy_rows if row.get("doc_id")}
    all_source_rows = classified_rows + review_rows

    output_rows: list[dict[str, Any]] = []

    for target_label in needed_labels:
        existing_candidates = [
            attach_existing_metadata(row, target_label, "existing_output")
            for row in all_source_rows
            if row.get("final_label") == target_label
        ]
        existing_candidates = rank_existing_candidates(existing_candidates)[: args.max_existing_per_label]
        exclude_doc_ids = base_doc_ids | {
            str(row.get("doc_id")) for row in existing_candidates if row.get("doc_id")
        }
        keyword_candidates = mine_keyword_candidates(
            raw_articles,
            target_label=target_label,
            exclude_doc_ids=exclude_doc_ids,
            limit=args.max_keyword_per_label,
        )
        output_rows.extend(existing_candidates)
        output_rows.extend(keyword_candidates)

        print(
            f"{target_label}: current={label_counts.get(target_label, 0)} "
            f"existing={len(existing_candidates)} keyword={len(keyword_candidates)}"
        )

    write_jsonl(resolve_path(args.output), output_rows)
    print(f"wrote {len(output_rows)} candidate rows to {resolve_path(args.output)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
