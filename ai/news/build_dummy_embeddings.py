#!/usr/bin/env python3
"""Create and cache GMS embeddings for dummy-news training rows."""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

from news_classifier_utils import (
    GOOGLE_EMBEDDING_ENDPOINT,
    GOOGLE_EMBEDDING_MODEL,
    compose_embedding_text,
    fetch_gms_embedding,
    load_jsonl_rows,
    resolve_path,
    sha1_text,
    write_jsonl,
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Build cached GMS embeddings for a JSONL training dataset."
    )
    parser.add_argument("--input", default="outputs/dummy_news.jsonl")
    parser.add_argument("--output", default="outputs/dummy_news_embeddings.jsonl")
    parser.add_argument("--api-key-env", default="GMS_KEY")
    parser.add_argument("--endpoint", default=GOOGLE_EMBEDDING_ENDPOINT)
    parser.add_argument("--model", default=GOOGLE_EMBEDDING_MODEL)
    parser.add_argument("--timeout-seconds", type=int, default=120)
    parser.add_argument("--start-index", type=int, default=0)
    parser.add_argument("--limit", type=int, default=None)
    return parser.parse_args()


def load_cache(path: Path) -> dict[str, dict]:
    rows = load_jsonl_rows(path)
    cache: dict[str, dict] = {}
    for row in rows:
        doc_id = str(row.get("doc_id", ""))
        if doc_id:
            cache[doc_id] = row
    return cache


def main() -> int:
    args = parse_args()
    input_path = resolve_path(args.input)
    output_path = resolve_path(args.output)

    if not input_path.exists():
        print(f"input file not found: {input_path}", file=sys.stderr)
        return 1

    api_key = os.environ.get(args.api_key_env)
    if not api_key:
        print(f"{args.api_key_env} environment variable is required.", file=sys.stderr)
        return 1

    rows = load_jsonl_rows(input_path)
    selected_rows = rows[args.start_index :]
    if args.limit is not None:
        selected_rows = selected_rows[: args.limit]

    cache = load_cache(output_path)
    updated_cache = dict(cache)

    processed = 0
    skipped = 0
    for row in selected_rows:
        doc_id = str(row.get("doc_id", ""))
        if not doc_id:
            continue
        text = compose_embedding_text(row)
        text_sha1 = sha1_text(text)
        cached = updated_cache.get(doc_id)
        if (
            cached
            and cached.get("text_sha1") == text_sha1
            and cached.get("embedding_model") == args.model
            and cached.get("embedding_endpoint") == args.endpoint
        ):
            skipped += 1
            print(f"[skip] {doc_id}")
            continue

        embedding = fetch_gms_embedding(
            text,
            api_key=api_key,
            endpoint=args.endpoint,
            model=args.model,
            timeout_seconds=args.timeout_seconds,
        )
        updated_cache[doc_id] = {
            "doc_id": doc_id,
            "doc_title": row.get("doc_title"),
            "final_label": row.get("final_label"),
            "embedding_model": args.model,
            "embedding_endpoint": args.endpoint,
            "text_sha1": text_sha1,
            "embedding_dimensions": len(embedding),
            "embedding": embedding,
        }
        processed += 1
        print(f"[{processed}] embedded {doc_id} dim={len(embedding)}")

    ordered_doc_ids = [str(row.get("doc_id", "")) for row in rows if row.get("doc_id")]
    remaining_doc_ids = sorted(set(updated_cache) - set(ordered_doc_ids))
    final_rows = [updated_cache[doc_id] for doc_id in ordered_doc_ids if doc_id in updated_cache]
    final_rows.extend(updated_cache[doc_id] for doc_id in remaining_doc_ids)
    write_jsonl(output_path, final_rows)
    print(
        f"completed processed={processed} skipped={skipped} "
        f"cached={len(final_rows)} output={output_path}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
