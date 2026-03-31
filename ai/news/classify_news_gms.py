#!/usr/bin/env python3
"""Batch-classify Korean economic news with SSAFY GMS gpt-5.2.

The script reads `경제_뉴스.json`, sends batched articles to an OpenAI-compatible
chat completions endpoint, validates the JSON response, and writes results into:

- classified.jsonl
- review.jsonl
- failed_batches.jsonl
- run_state.json

API credentials are read from the environment only.
"""

from __future__ import annotations

import argparse
import concurrent.futures
import json
import os
import re
import subprocess
import sys
import time
import threading
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any
from urllib import error


DEFAULT_ENDPOINT = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
DEFAULT_MODEL = "gpt-4.1-nano"
DEFAULT_BATCH_SIZE = 20
DEFAULT_WORKERS = 4
DEFAULT_CONFIDENCE_THRESHOLD = 0.75
DEFAULT_TIMEOUT_SECONDS = 180
DEFAULT_MAX_RETRIES = 2
DEFAULT_MAX_TOKENS = 4096

ALLOWED_STATES = {"CRISIS", "RECOVERY", "BOOM"}
ALLOWED_LABELS = {
    "CRISIS_TO_CRISIS",
    "CRISIS_TO_RECOVERY",
    "CRISIS_TO_BOOM",
    "RECOVERY_TO_CRISIS",
    "RECOVERY_TO_RECOVERY",
    "RECOVERY_TO_BOOM",
    "BOOM_TO_CRISIS",
    "BOOM_TO_RECOVERY",
    "BOOM_TO_BOOM",
}

DEVELOPER_PROMPT = """당신은 한국어 경제뉴스 전이 분류기다.

목표:
- 각 기사의 현재 경제 상태(from_state)와 다음 1~3턴의 경제 방향(to_state)을 분류한다.
- 최종 라벨(final_label)은 반드시 "{from_state}_TO_{to_state}" 형식이어야 한다.

상태 정의:
- CRISIS: 급감, 위축, 중단, 적자, 구조조정, 실업, 자금경색, 불확실성 확대
- RECOVERY: 안정화, 점진 반등, 회복 조짐, 정상화, 감소세 둔화
- BOOM: 급증, 활황, 과열, 투자 확대, 실적 급증, 자산가격 강세

판정 규칙:
1. from_state는 기사 시점의 현재 경제 상태다.
2. to_state는 이 뉴스가 시사하는 다음 1~3턴의 경제 방향이다.
3. 미래 신호가 약하면 to_state = from_state 로 둔다.
4. 기업 단건 기사라도 수요, 고용, 생산, 투자, 수출, 금리, 부동산 등 거시 신호가 드러나면 분류한다.
5. 거시 신호가 약하거나 방향이 불명확하면 should_review 를 true 로 둔다.
6. confidence는 정답 확률이 아니라 모델의 자기 확신도다. 0.0~1.0 사이의 숫자로 반환한다.
7. reason은 한국어 1~2문장으로 짧게 쓴다.

반드시 JSON만 반환하라. Markdown, 코드펜스, 설명문을 절대 추가하지 마라.

반환 형식:
{
  "results": [
    {
      "doc_id": "원본과 동일한 문자열",
      "from_state": "CRISIS|RECOVERY|BOOM",
      "to_state": "CRISIS|RECOVERY|BOOM",
      "final_label": "CRISIS_TO_CRISIS|CRISIS_TO_RECOVERY|CRISIS_TO_BOOM|RECOVERY_TO_CRISIS|RECOVERY_TO_RECOVERY|RECOVERY_TO_BOOM|BOOM_TO_CRISIS|BOOM_TO_RECOVERY|BOOM_TO_BOOM",
      "confidence": 0.0,
      "should_review": false,
      "reason": "짧은 근거"
    }
  ]
}
"""


class AuthenticationError(Exception):
    """Raised when the API key is invalid or unauthorized."""


APPEND_LOCK = threading.Lock()


@dataclass
class Article:
    doc_id: str
    doc_title: str
    doc_source: str
    doc_published: int
    article_text: str


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Classify Korean economic news using SSAFY GMS."
    )
    parser.add_argument(
        "--input",
        default="경제_뉴스.json",
        help="Path to the input JSON file.",
    )
    parser.add_argument(
        "--output-dir",
        default="outputs/news_classification",
        help="Directory to store JSONL outputs and state files.",
    )
    parser.add_argument(
        "--endpoint",
        default=DEFAULT_ENDPOINT,
        help="OpenAI-compatible chat completions endpoint.",
    )
    parser.add_argument(
        "--model",
        default=DEFAULT_MODEL,
        help="Model name to send to the endpoint.",
    )
    parser.add_argument(
        "--max-tokens",
        type=int,
        default=DEFAULT_MAX_TOKENS,
        help="Maximum output tokens per request.",
    )
    parser.add_argument(
        "--api-key-env",
        default="GMS_KEY",
        help="Environment variable name that stores the API key.",
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=DEFAULT_BATCH_SIZE,
        help="Number of articles per request.",
    )
    parser.add_argument(
        "--workers",
        type=int,
        default=DEFAULT_WORKERS,
        help="Number of batches to process in parallel.",
    )
    parser.add_argument(
        "--confidence-threshold",
        type=float,
        default=DEFAULT_CONFIDENCE_THRESHOLD,
        help="Confidence threshold below which records are sent to review.",
    )
    parser.add_argument(
        "--start-index",
        type=int,
        default=0,
        help="Start processing from this input index.",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Maximum number of articles to process after start-index.",
    )
    parser.add_argument(
        "--max-retries",
        type=int,
        default=DEFAULT_MAX_RETRIES,
        help="Retries per batch before splitting into smaller batches.",
    )
    parser.add_argument(
        "--timeout-seconds",
        type=int,
        default=DEFAULT_TIMEOUT_SECONDS,
        help="HTTP timeout per request.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Build the first request and print a summary without calling the API.",
    )
    return parser.parse_args()


def utc_now_iso() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def normalize_text(text: str) -> str:
    text = text.replace("\u00a0", " ")
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def load_articles(input_path: Path) -> list[Article]:
    with input_path.open("r", encoding="utf-8") as f:
        payload = json.load(f)

    articles: list[Article] = []
    for item in payload.get("data", []):
        article_text = "\n".join(
            paragraph.get("context", "") for paragraph in item.get("paragraphs", [])
        )
        articles.append(
            Article(
                doc_id=str(item["doc_id"]),
                doc_title=normalize_text(item.get("doc_title", "")),
                doc_source=normalize_text(item.get("doc_source", "")),
                doc_published=int(item.get("doc_published", 0)),
                article_text=normalize_text(article_text),
            )
        )
    return articles


def ensure_output_dir(output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)


def load_processed_ids(path: Path) -> set[str]:
    processed: set[str] = set()
    if not path.exists():
        return processed

    with path.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                row = json.loads(line)
            except json.JSONDecodeError:
                continue
            doc_id = row.get("doc_id")
            if doc_id:
                processed.add(str(doc_id))
    return processed


def append_jsonl(path: Path, rows: list[dict[str, Any]]) -> None:
    if not rows:
        return
    with APPEND_LOCK:
        with path.open("a", encoding="utf-8") as f:
            for row in rows:
                f.write(json.dumps(row, ensure_ascii=False) + "\n")


def save_state(path: Path, state: dict[str, Any]) -> None:
    with path.open("w", encoding="utf-8") as f:
        json.dump(state, f, ensure_ascii=False, indent=2)


def extract_json_payload(raw_text: str) -> Any:
    raw_text = raw_text.strip()
    if not raw_text:
        raise ValueError("Model response was empty.")

    fenced = re.findall(r"```(?:json)?\s*(.*?)```", raw_text, flags=re.DOTALL)
    candidates = fenced + [raw_text]

    for candidate in candidates:
        candidate = candidate.strip()
        if not candidate:
            continue
        try:
            return json.loads(candidate)
        except json.JSONDecodeError:
            pass

        start_positions = [pos for pos in (candidate.find("{"), candidate.find("[")) if pos != -1]
        if not start_positions:
            continue
        start = min(start_positions)

        for end_char in ("}", "]"):
            end = candidate.rfind(end_char)
            if end == -1 or end < start:
                continue
            snippet = candidate[start : end + 1]
            try:
                return json.loads(snippet)
            except json.JSONDecodeError:
                continue

    raise ValueError("Could not parse JSON from model response.")


def clamp_confidence(value: Any) -> float:
    if isinstance(value, bool):
        raise ValueError("confidence must be numeric, not boolean")
    number = float(value)
    if number < 0 or number > 1:
        raise ValueError(f"confidence out of range: {number}")
    return number


def validate_result_item(item: dict[str, Any], batch_ids: set[str]) -> dict[str, Any]:
    doc_id = str(item["doc_id"])
    if doc_id not in batch_ids:
        raise ValueError(f"Unexpected doc_id in response: {doc_id}")

    from_state = str(item["from_state"]).strip().upper()
    to_state = str(item["to_state"]).strip().upper()
    if from_state not in ALLOWED_STATES:
        raise ValueError(f"Invalid from_state for {doc_id}: {from_state}")
    if to_state not in ALLOWED_STATES:
        raise ValueError(f"Invalid to_state for {doc_id}: {to_state}")

    expected_label = f"{from_state}_TO_{to_state}"
    final_label = str(item["final_label"]).strip().upper()
    if final_label not in ALLOWED_LABELS:
        raise ValueError(f"Invalid final_label for {doc_id}: {final_label}")
    if final_label != expected_label:
        raise ValueError(
            f"final_label mismatch for {doc_id}: expected {expected_label}, got {final_label}"
        )

    confidence = clamp_confidence(item["confidence"])
    should_review = item["should_review"]
    if not isinstance(should_review, bool):
        raise ValueError(f"should_review must be boolean for {doc_id}")

    reason = normalize_text(str(item["reason"]))
    if not reason:
        raise ValueError(f"reason must not be empty for {doc_id}")

    return {
        "doc_id": doc_id,
        "from_state": from_state,
        "to_state": to_state,
        "final_label": final_label,
        "confidence": confidence,
        "should_review": should_review,
        "reason": reason,
    }


def validate_batch_response(response_text: str, batch: list[Article]) -> list[dict[str, Any]]:
    payload = extract_json_payload(response_text)
    if isinstance(payload, list):
        results = payload
    elif isinstance(payload, dict) and isinstance(payload.get("results"), list):
        results = payload["results"]
    else:
        raise ValueError("Response must be a JSON object with a results array.")

    batch_ids = {article.doc_id for article in batch}
    if len(results) != len(batch):
        raise ValueError(f"Expected {len(batch)} results, got {len(results)}")

    validated = [validate_result_item(item, batch_ids) for item in results]
    seen_ids: set[str] = set()
    for item in validated:
        if item["doc_id"] in seen_ids:
            raise ValueError(f"Duplicate doc_id in response: {item['doc_id']}")
        seen_ids.add(item["doc_id"])

    missing = batch_ids - seen_ids
    if missing:
        raise ValueError(f"Missing doc_ids in response: {sorted(missing)}")

    validated.sort(key=lambda row: next(i for i, article in enumerate(batch) if article.doc_id == row["doc_id"]))
    return validated


def build_messages(batch: list[Article]) -> list[dict[str, str]]:
    user_payload = {
        "articles": [
            {
                "doc_id": article.doc_id,
                "headline": article.doc_title,
                "article_text": article.article_text,
            }
            for article in batch
        ]
    }

    user_prompt = (
        "다음 기사들을 분류하라. 각 기사에 대해 반드시 하나의 결과를 반환하고, "
        "doc_id는 입력과 동일하게 유지하라.\n"
        + json.dumps(user_payload, ensure_ascii=False)
    )
    return [
        {"role": "developer", "content": DEVELOPER_PROMPT},
        {"role": "user", "content": user_prompt},
    ]


def build_request_body(
    model: str,
    messages: list[dict[str, str]],
    max_tokens: int,
) -> bytes:
    body = {
        "model": model,
        "temperature": 0,
        "max_tokens": max_tokens,
        "messages": messages,
    }
    return json.dumps(body, ensure_ascii=False).encode("utf-8")


def call_chat_completions(
    *,
    endpoint: str,
    api_key: str,
    timeout_seconds: int,
    body: bytes,
) -> str:
    command = [
        "curl",
        "--silent",
        "--show-error",
        "--http1.1",
        "--location",
        "--connect-timeout",
        "20",
        "--max-time",
        str(timeout_seconds),
        "--retry",
        "2",
        "--retry-all-errors",
        "-H",
        "Content-Type: application/json",
        "-H",
        f"Authorization: Bearer {api_key}",
        "--data-binary",
        "@-",
        "--write-out",
        "\n__HTTP_STATUS__:%{http_code}",
        endpoint,
    ]
    completed = subprocess.run(
        command,
        input=body,
        capture_output=True,
        check=False,
    )

    stdout = completed.stdout.decode("utf-8", errors="replace")
    stderr = completed.stderr.decode("utf-8", errors="replace").strip()

    marker = "\n__HTTP_STATUS__:"
    if marker not in stdout:
        detail = stderr or stdout[-500:]
        raise error.URLError(f"curl response missing HTTP status marker: {detail}")

    raw, status_text = stdout.rsplit(marker, 1)
    try:
        status_code = int(status_text.strip())
    except ValueError as exc:
        raise error.URLError(f"Invalid HTTP status marker: {status_text!r}") from exc

    raw = raw.strip()
    if status_code in {401, 403}:
        raise AuthenticationError(
            f"Authentication failed with HTTP {status_code}: {raw[:500]}"
        )
    if completed.returncode != 0:
        detail = stderr or raw[:500]
        raise error.URLError(f"curl exited with code {completed.returncode}: {detail}")
    if status_code >= 400:
        raise error.URLError(f"HTTP {status_code}: {raw[:500]}")

    payload = json.loads(raw)
    try:
        content = payload["choices"][0]["message"]["content"]
    except (KeyError, IndexError, TypeError) as exc:
        raise ValueError(f"Unexpected API response shape: {raw[:500]}") from exc
    if not isinstance(content, str):
        raise ValueError("Model content must be a string.")
    return content


def classify_batch(
    *,
    batch: list[Article],
    endpoint: str,
    api_key: str,
    model: str,
    max_tokens: int,
    timeout_seconds: int,
    max_retries: int,
    confidence_threshold: float,
    failed_path: Path,
) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    messages = build_messages(batch)
    body = build_request_body(model, messages, max_tokens)

    last_error: Exception | None = None
    for attempt in range(max_retries + 1):
        try:
            response_text = call_chat_completions(
                endpoint=endpoint,
                api_key=api_key,
                timeout_seconds=timeout_seconds,
                body=body,
            )
            validated = validate_batch_response(response_text, batch)
            classified: list[dict[str, Any]] = []
            review: list[dict[str, Any]] = []
            article_map = {article.doc_id: article for article in batch}

            for row in validated:
                article = article_map[row["doc_id"]]
                merged = {
                    "doc_id": article.doc_id,
                    "doc_title": article.doc_title,
                    "doc_source": article.doc_source,
                    "doc_published": article.doc_published,
                    "article_text": article.article_text,
                    **row,
                }
                if row["should_review"] or row["confidence"] < confidence_threshold:
                    review.append(merged)
                else:
                    classified.append(merged)
            return classified, review
        except (error.HTTPError, error.URLError, TimeoutError, ValueError, json.JSONDecodeError) as exc:
            last_error = exc
            if attempt < max_retries:
                time.sleep(2**attempt)
                continue

    if len(batch) > 1:
        midpoint = max(1, len(batch) // 2)
        left = batch[:midpoint]
        right = batch[midpoint:]
        left_classified, left_review = classify_batch(
            batch=left,
            endpoint=endpoint,
            api_key=api_key,
            model=model,
            max_tokens=max_tokens,
            timeout_seconds=timeout_seconds,
            max_retries=max_retries,
            confidence_threshold=confidence_threshold,
            failed_path=failed_path,
        )
        right_classified, right_review = classify_batch(
            batch=right,
            endpoint=endpoint,
            api_key=api_key,
            model=model,
            max_tokens=max_tokens,
            timeout_seconds=timeout_seconds,
            max_retries=max_retries,
            confidence_threshold=confidence_threshold,
            failed_path=failed_path,
        )
        return left_classified + right_classified, left_review + right_review

    article = batch[0]
    failure_row = {
        "failed_at": utc_now_iso(),
        "doc_id": article.doc_id,
        "doc_title": article.doc_title,
        "error": repr(last_error),
    }
    append_jsonl(failed_path, [failure_row])
    return [], [
        {
            "doc_id": article.doc_id,
            "doc_title": article.doc_title,
            "doc_source": article.doc_source,
            "doc_published": article.doc_published,
            "article_text": article.article_text,
            "from_state": None,
            "to_state": None,
            "final_label": None,
            "confidence": 0.0,
            "should_review": True,
            "reason": f"API or validation failure: {last_error}",
        }
    ]


def batched(items: list[Article], size: int) -> list[list[Article]]:
    return [items[i : i + size] for i in range(0, len(items), size)]


def summarize_articles(articles: list[Article]) -> dict[str, Any]:
    if not articles:
        return {"count": 0}
    lengths = [len(article.article_text) for article in articles]
    lengths_sorted = sorted(lengths)
    mid = len(lengths_sorted) // 2
    median = lengths_sorted[mid] if len(lengths_sorted) % 2 else (
        lengths_sorted[mid - 1] + lengths_sorted[mid]
    ) / 2
    return {
        "count": len(articles),
        "min_length": min(lengths),
        "median_length": median,
        "max_length": max(lengths),
    }


def save_progress_state(
    *,
    path: Path,
    started_at: str,
    args: argparse.Namespace,
    input_path: Path,
    output_dir: Path,
    selected_count: int,
    total_pending: int,
    processed_count: int,
    classified_count: int,
    review_count: int,
    total_batches: int,
    status: str,
    last_completed_batch: int | None = None,
    finished_at: str | None = None,
    failed_at: str | None = None,
    error_message: str | None = None,
) -> None:
    payload: dict[str, Any] = {
        "started_at": started_at,
        "input_path": str(input_path),
        "output_dir": str(output_dir),
        "model": args.model,
        "max_tokens": args.max_tokens,
        "endpoint": args.endpoint,
        "batch_size": args.batch_size,
        "workers": args.workers,
        "confidence_threshold": args.confidence_threshold,
        "start_index": args.start_index,
        "limit": args.limit,
        "total_selected": selected_count,
        "total_pending": total_pending,
        "processed_count": processed_count,
        "classified_count": classified_count,
        "review_count": review_count,
        "total_batches": total_batches,
        "status": status,
        "updated_at": utc_now_iso(),
    }
    if last_completed_batch is not None:
        payload["last_completed_batch"] = last_completed_batch
    if finished_at is not None:
        payload["finished_at"] = finished_at
    if failed_at is not None:
        payload["failed_at"] = failed_at
    if error_message is not None:
        payload["error"] = error_message
    save_state(path, payload)


def main() -> int:
    args = parse_args()

    input_path = Path(args.input)
    output_dir = Path(args.output_dir)
    classified_path = output_dir / "classified.jsonl"
    review_path = output_dir / "review.jsonl"
    failed_path = output_dir / "failed_batches.jsonl"
    state_path = output_dir / "run_state.json"

    articles = load_articles(input_path)
    selected = articles[args.start_index :]
    if args.limit is not None:
        selected = selected[: args.limit]

    ensure_output_dir(output_dir)

    processed_ids = load_processed_ids(classified_path) | load_processed_ids(review_path)
    pending = [article for article in selected if article.doc_id not in processed_ids]

    if args.dry_run:
        sample_batch = pending[: args.batch_size]
        if not sample_batch:
            print("No pending articles found for dry-run.")
            return 0
        messages = build_messages(sample_batch)
        body = build_request_body(args.model, messages, args.max_tokens)
        summary = summarize_articles(sample_batch)
        print(
            json.dumps(
                {
                    "dry_run": True,
                    "model": args.model,
                    "max_tokens": args.max_tokens,
                    "endpoint": args.endpoint,
                    "batch_size": len(sample_batch),
                    "payload_bytes": len(body),
                    "article_summary": summary,
                    "first_doc_id": sample_batch[0].doc_id,
                },
                ensure_ascii=False,
                indent=2,
            )
        )
        return 0

    api_key = os.environ.get(args.api_key_env)
    if not api_key:
        print(
            f"Missing API key. Set the {args.api_key_env} environment variable.",
            file=sys.stderr,
        )
        return 2

    total_pending = len(pending)
    batches = batched(pending, args.batch_size)
    started_at = utc_now_iso()
    processed_count = 0
    classified_count = 0
    review_count = 0

    save_progress_state(
        path=state_path,
        started_at=started_at,
        args=args,
        input_path=input_path,
        output_dir=output_dir,
        selected_count=len(selected),
        total_pending=total_pending,
        processed_count=0,
        classified_count=0,
        review_count=0,
        total_batches=len(batches),
        status="running",
    )

    completed_batches = 0
    workers = max(1, args.workers)

    def handle_batch_result(
        *,
        batch_index: int,
        batch: list[Article],
        classified_rows: list[dict[str, Any]],
        review_rows: list[dict[str, Any]],
    ) -> None:
        nonlocal processed_count, classified_count, review_count, completed_batches

        append_jsonl(classified_path, classified_rows)
        append_jsonl(review_path, review_rows)

        processed_count += len(batch)
        classified_count += len(classified_rows)
        review_count += len(review_rows)
        completed_batches += 1

        save_progress_state(
            path=state_path,
            started_at=started_at,
            args=args,
            input_path=input_path,
            output_dir=output_dir,
            selected_count=len(selected),
            total_pending=total_pending,
            processed_count=processed_count,
            classified_count=classified_count,
            review_count=review_count,
            total_batches=len(batches),
            status="running",
            last_completed_batch=batch_index,
        )

        print(
            f"[{completed_batches}/{len(batches)}] batch={batch_index} "
            f"processed={processed_count}/{total_pending} "
            f"classified={classified_count} review={review_count}",
            flush=True,
        )

    if workers == 1:
        for batch_index, batch in enumerate(batches, start=1):
            try:
                classified_rows, review_rows = classify_batch(
                batch=batch,
                endpoint=args.endpoint,
                api_key=api_key,
                model=args.model,
                max_tokens=args.max_tokens,
                timeout_seconds=args.timeout_seconds,
                max_retries=args.max_retries,
                confidence_threshold=args.confidence_threshold,
                    failed_path=failed_path,
                )
            except AuthenticationError as exc:
                save_progress_state(
                    path=state_path,
                    started_at=started_at,
                    args=args,
                    input_path=input_path,
                    output_dir=output_dir,
                    selected_count=len(selected),
                    total_pending=total_pending,
                    processed_count=processed_count,
                    classified_count=classified_count,
                    review_count=review_count,
                    total_batches=len(batches),
                    status="failed_auth",
                    last_completed_batch=batch_index - 1,
                    failed_at=utc_now_iso(),
                    error_message=str(exc),
                )
                print(str(exc), file=sys.stderr)
                return 3

            handle_batch_result(
                batch_index=batch_index,
                batch=batch,
                classified_rows=classified_rows,
                review_rows=review_rows,
            )
    else:
        future_map: dict[concurrent.futures.Future[Any], tuple[int, list[Article]]] = {}
        with concurrent.futures.ThreadPoolExecutor(max_workers=workers) as executor:
            for batch_index, batch in enumerate(batches, start=1):
                future = executor.submit(
                    classify_batch,
                    batch=batch,
                    endpoint=args.endpoint,
                    api_key=api_key,
                    model=args.model,
                    max_tokens=args.max_tokens,
                    timeout_seconds=args.timeout_seconds,
                    max_retries=args.max_retries,
                    confidence_threshold=args.confidence_threshold,
                    failed_path=failed_path,
                )
                future_map[future] = (batch_index, batch)

            for future in concurrent.futures.as_completed(future_map):
                batch_index, batch = future_map[future]
                try:
                    classified_rows, review_rows = future.result()
                except AuthenticationError as exc:
                    for pending_future in future_map:
                        pending_future.cancel()
                    save_progress_state(
                        path=state_path,
                        started_at=started_at,
                        args=args,
                        input_path=input_path,
                        output_dir=output_dir,
                        selected_count=len(selected),
                        total_pending=total_pending,
                        processed_count=processed_count,
                        classified_count=classified_count,
                        review_count=review_count,
                        total_batches=len(batches),
                        status="failed_auth",
                        last_completed_batch=completed_batches,
                        failed_at=utc_now_iso(),
                        error_message=str(exc),
                    )
                    print(str(exc), file=sys.stderr)
                    return 3

                handle_batch_result(
                    batch_index=batch_index,
                    batch=batch,
                    classified_rows=classified_rows,
                    review_rows=review_rows,
                )

    save_progress_state(
        path=state_path,
        started_at=started_at,
        args=args,
        input_path=input_path,
        output_dir=output_dir,
        selected_count=len(selected),
        total_pending=total_pending,
        processed_count=processed_count,
        classified_count=classified_count,
        review_count=review_count,
        total_batches=len(batches),
        status="completed",
        finished_at=utc_now_iso(),
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
