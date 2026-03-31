#!/usr/bin/env python3
"""Load classified news JSONL into news_master and store GMS embeddings."""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
from decimal import Decimal
from pathlib import Path
from typing import Any
from urllib.parse import urlencode


EMBEDDING_ENDPOINT = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", default="outputs/dummy_news.jsonl")
    parser.add_argument("--container", default=os.environ.get("DB_CONTAINER_NAME", "homerun-local-postgres"))
    parser.add_argument("--db-name", default=os.environ.get("DB_NAME", "homerun"))
    parser.add_argument("--db-user", default=os.environ.get("DB_USER", "homerun_user"))
    parser.add_argument("--model", default="text-embedding-3-small")
    parser.add_argument("--endpoint", default=EMBEDDING_ENDPOINT)
    parser.add_argument("--limit", type=int, default=None)
    parser.add_argument("--skip-existing-embedding", action="store_true")
    parser.add_argument("--upsert-without-embedding", action="store_true")
    return parser.parse_args()


def resolve_input_path(raw_path: str) -> Path:
    path = Path(raw_path)
    if path.is_absolute():
        return path
    return Path(__file__).resolve().parent / path


def run_command(command: list[str], *, input_text: str | None = None) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        command,
        input=input_text,
        text=True,
        capture_output=True,
        check=False,
    )


def sql_literal(value: Any) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    if isinstance(value, (int, float, Decimal)):
        return str(value)
    text = str(value).replace("'", "''")
    return f"'{text}'"


def vector_literal(values: list[float]) -> str:
    text = ",".join(format(float(value), ".12g") for value in values)
    return f"'[{text}]'::vector"


def normalize_article_text(value: Any) -> str | None:
    if value is None:
        return None
    text = str(value)
    text = re.sub(r"\s*\n+\s*", " ", text)
    text = re.sub(r"[ \t]{2,}", " ", text)
    return text.strip()


def derive_sentiment(final_label: str | None) -> str | None:
    if not final_label:
        return None
    if final_label.endswith("_TO_CRISIS"):
        return "negative"
    if final_label.endswith("_TO_BOOM"):
        return "positive"
    if final_label.endswith("_TO_RECOVERY"):
        return "mixed"
    return None


def extract_embedding(payload: dict[str, Any]) -> list[float]:
    if isinstance(payload.get("data"), list) and payload["data"]:
        first = payload["data"][0]
        if isinstance(first, dict):
            values = first.get("embedding")
            if isinstance(values, list):
                return [float(value) for value in values]
    if isinstance(payload.get("embedding"), dict):
        values = payload["embedding"].get("values")
        if isinstance(values, list):
            return [float(value) for value in values]
    if isinstance(payload.get("embeddings"), list) and payload["embeddings"]:
        first = payload["embeddings"][0]
        if isinstance(first, dict):
            values = first.get("values")
            if isinstance(values, list):
                return [float(value) for value in values]
    raise ValueError(f"Unexpected embedding response: {payload}")


def fetch_embedding(text: str, model_name: str, gms_key: str, endpoint: str) -> list[float]:
    if "api.openai.com/v1/embeddings" in endpoint:
        payload = {"model": model_name, "input": text}
        command = [
            "curl",
            "--silent",
            "--show-error",
            "--fail",
            endpoint,
            "-H",
            "Content-Type: application/json",
            "-H",
            f"Authorization: Bearer {gms_key}",
            "-d",
            json.dumps(payload, ensure_ascii=False),
        ]
    else:
        url = f"{endpoint}?{urlencode({'key': gms_key})}"
        payload = {
            "model": model_name,
            "content": {"parts": [{"text": text}]},
        }
        command = [
            "curl",
            "--silent",
            "--show-error",
            "--fail",
            "-X",
            "POST",
            url,
            "-H",
            "Content-Type: application/json",
            "-d",
            json.dumps(payload, ensure_ascii=False),
        ]
    result = run_command(command)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip() or "embedding request failed")
    return extract_embedding(json.loads(result.stdout))


def embedding_exists(container: str, db_user: str, db_name: str, news_id: str) -> bool:
    sql = (
        "SELECT CASE WHEN EXISTS ("
        f"SELECT 1 FROM news_master WHERE news_id = {sql_literal(news_id)} AND embedding IS NOT NULL"
        ") THEN 1 ELSE 0 END;"
    )
    command = [
        "docker",
        "exec",
        "-i",
        container,
        "psql",
        "-t",
        "-A",
        "-v",
        "ON_ERROR_STOP=1",
        "-U",
        db_user,
        "-d",
        db_name,
        "-c",
        sql,
    ]
    result = run_command(command)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip() or "failed to query existing embedding")
    return result.stdout.strip() == "1"


def upsert_news_row(
    article: dict[str, Any],
    embedding: list[float] | None,
    *,
    container: str,
    db_user: str,
    db_name: str,
) -> None:
    sentiment = derive_sentiment(article.get("final_label"))
    economic_cycle_type = article.get("final_label")
    article_text = normalize_article_text(article.get("article_text"))
    embedding_dimensions = len(embedding) if embedding is not None else None
    embedding_value = vector_literal(embedding) if embedding is not None else "NULL"
    combined_sql = f"""
INSERT INTO news_master (
  news_id,
  title,
  sentiment,
  source_name,
  article_text,
  economic_cycle_type,
  reason,
  sector_impact,
  exchange_rate_impact,
  real_estate_impact,
  job_impact,
  embedding_dimensions,
  embedding
) VALUES (
  {sql_literal(article.get('doc_id'))},
  {sql_literal(article.get('doc_title'))},
  {sql_literal(sentiment)},
  {sql_literal(article.get('doc_source'))},
  {sql_literal(article_text)},
  {sql_literal(economic_cycle_type)},
  {sql_literal(article.get('reason'))},
  NULL,
  NULL,
  NULL,
  NULL,
  {sql_literal(embedding_dimensions)},
  {embedding_value}
)
ON CONFLICT (news_id) DO UPDATE SET
  title = EXCLUDED.title,
  sentiment = EXCLUDED.sentiment,
  source_name = EXCLUDED.source_name,
  article_text = EXCLUDED.article_text,
  economic_cycle_type = EXCLUDED.economic_cycle_type,
  reason = EXCLUDED.reason,
  embedding_dimensions = COALESCE(EXCLUDED.embedding_dimensions, news_master.embedding_dimensions),
  embedding = COALESCE(EXCLUDED.embedding, news_master.embedding);
"""
    command = [
        "docker",
        "exec",
        "-i",
        container,
        "psql",
        "-v",
        "ON_ERROR_STOP=1",
        "-U",
        db_user,
        "-d",
        db_name,
    ]
    result = run_command(command, input_text=combined_sql)
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip() or "failed to upsert news row")


def main() -> int:
    args = parse_args()
    input_path = resolve_input_path(args.input)
    gms_key = os.environ.get("GMS_KEY")
    if not gms_key and not args.upsert_without_embedding:
        print("GMS_KEY environment variable is required.", file=sys.stderr)
        return 1
    if not input_path.exists():
        print(f"Input file not found: {input_path}", file=sys.stderr)
        return 1

    processed_count = 0
    skipped_count = 0

    with input_path.open("r", encoding="utf-8") as handle:
        for line in handle:
            if args.limit is not None and processed_count >= args.limit:
                break
            article = json.loads(line)
            news_id = article["doc_id"]
            if args.skip_existing_embedding and embedding_exists(args.container, args.db_user, args.db_name, news_id):
                skipped_count += 1
                print(f"[skip] {news_id}")
                continue

            embedding = None
            if not args.upsert_without_embedding:
                article_text = normalize_article_text(article.get("article_text")) or ""
                embedding_input = f"{article.get('doc_title', '').strip()}\n\n{article_text}".strip()
                embedding = fetch_embedding(embedding_input, args.model, gms_key, args.endpoint)
            upsert_news_row(
                article,
                embedding,
                container=args.container,
                db_user=args.db_user,
                db_name=args.db_name,
            )
            processed_count += 1
            if embedding is None:
                print(f"[{processed_count}] upserted {news_id} without embedding")
            else:
                print(f"[{processed_count}] upserted {news_id} dim={len(embedding)}")

    print(f"completed processed={processed_count} skipped={skipped_count}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
