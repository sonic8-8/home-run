#!/usr/bin/env python3
"""Shared helpers for dummy-news embedding and classification workflows."""

from __future__ import annotations

import hashlib
import json
import re
import subprocess
from pathlib import Path
from typing import Any
from urllib.parse import urlencode


LABEL_ORDER = [
    "CRISIS_TO_CRISIS",
    "CRISIS_TO_RECOVERY",
    "CRISIS_TO_BOOM",
    "RECOVERY_TO_CRISIS",
    "RECOVERY_TO_RECOVERY",
    "RECOVERY_TO_BOOM",
    "BOOM_TO_CRISIS",
    "BOOM_TO_RECOVERY",
    "BOOM_TO_BOOM",
]
ALLOWED_LABELS = set(LABEL_ORDER)

GOOGLE_EMBEDDING_ENDPOINT = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings"
GOOGLE_EMBEDDING_MODEL = "text-embedding-3-small"


def normalize_text(text: str) -> str:
    text = text.replace("\u00a0", " ")
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def resolve_path(raw_path: str) -> Path:
    path = Path(raw_path)
    if path.is_absolute():
        return path
    return Path(__file__).resolve().parent / path


def load_jsonl_rows(path: Path) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    if not path.exists():
        return rows
    with path.open("r", encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if not line:
                continue
            rows.append(json.loads(line))
    return rows


def write_jsonl(path: Path, rows: list[dict[str, Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        for row in rows:
            handle.write(json.dumps(row, ensure_ascii=False) + "\n")


def append_jsonl(path: Path, row: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a", encoding="utf-8") as handle:
        handle.write(json.dumps(row, ensure_ascii=False) + "\n")


def compose_embedding_text(row: dict[str, Any]) -> str:
    title = normalize_text(str(row.get("doc_title", "")))
    article_text = normalize_text(str(row.get("article_text", "")))
    return f"{title}\n\n{article_text}".strip()


def sha1_text(text: str) -> str:
    return hashlib.sha1(text.encode("utf-8")).hexdigest()


def load_articles_from_corpus(path: Path) -> dict[str, dict[str, Any]]:
    with path.open("r", encoding="utf-8") as handle:
        payload = json.load(handle)

    articles: dict[str, dict[str, Any]] = {}
    for item in payload.get("data", []):
        article_text = "\n".join(
            paragraph.get("context", "") for paragraph in item.get("paragraphs", [])
        )
        doc_id = str(item.get("doc_id"))
        articles[doc_id] = {
            "doc_id": doc_id,
            "doc_title": normalize_text(item.get("doc_title", "")),
            "doc_source": normalize_text(item.get("doc_source", "")),
            "doc_published": int(item.get("doc_published", 0)),
            "article_text": normalize_text(article_text),
        }
    return articles


def extract_embedding(payload: dict[str, Any]) -> list[float]:
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
    if isinstance(payload.get("data"), list) and payload["data"]:
        first = payload["data"][0]
        if isinstance(first, dict):
            values = first.get("embedding")
            if isinstance(values, list):
                return [float(value) for value in values]
    raise ValueError(f"Unexpected embedding response shape: {payload}")


def run_command(command: list[str]) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        command,
        text=True,
        capture_output=True,
        check=False,
    )


def build_curl_command(
    *,
    url: str,
    payload: dict[str, Any],
    timeout_seconds: int,
    api_key: str | None = None,
) -> list[str]:
    command = [
        "curl",
        "--silent",
        "--show-error",
        "--fail",
        "--max-time",
        str(timeout_seconds),
        "-X",
        "POST",
        url,
        "-H",
        "Content-Type: application/json",
    ]
    if api_key is not None:
        command.extend(["-H", f"Authorization: Bearer {api_key}"])
    command.extend(["-d", json.dumps(payload, ensure_ascii=False)])
    return command


def replace_google_model_in_endpoint(endpoint: str, model: str) -> str:
    model_name = model.removeprefix("models/")
    return re.sub(r"/models/[^/:]+:embedContent$", f"/models/{model_name}:embedContent", endpoint)


def fetch_gms_embedding(
    text: str,
    *,
    api_key: str,
    endpoint: str = GOOGLE_EMBEDDING_ENDPOINT,
    model: str = GOOGLE_EMBEDDING_MODEL,
    timeout_seconds: int = 120,
) -> list[float]:
    attempts: list[tuple[str, list[str]]] = []

    if "api.openai.com/v1/embeddings" in endpoint:
        attempts.append(
            (
                "openai_embeddings",
                build_curl_command(
                    url=endpoint,
                    payload={"model": model, "input": text},
                    timeout_seconds=timeout_seconds,
                    api_key=api_key,
                ),
            )
        )
    else:
        url = f"{endpoint}?{urlencode({'key': api_key})}"
        content = {"parts": [{"text": text}]}
        attempts.append(
            (
                "google_embed_with_model",
                build_curl_command(
                    url=url,
                    payload={"model": model, "content": content},
                    timeout_seconds=timeout_seconds,
                ),
            )
        )
        attempts.append(
            (
                "google_embed_without_model",
                build_curl_command(
                    url=url,
                    payload={"content": content},
                    timeout_seconds=timeout_seconds,
                ),
            )
        )
        alt_endpoint = replace_google_model_in_endpoint(endpoint, model)
        if alt_endpoint != endpoint:
            alt_url = f"{alt_endpoint}?{urlencode({'key': api_key})}"
            attempts.append(
                (
                    "google_embed_alt_path_with_model",
                    build_curl_command(
                        url=alt_url,
                        payload={"model": model, "content": content},
                        timeout_seconds=timeout_seconds,
                    ),
                )
            )
            attempts.append(
                (
                    "google_embed_alt_path_without_model",
                    build_curl_command(
                        url=alt_url,
                        payload={"content": content},
                        timeout_seconds=timeout_seconds,
                    ),
                )
            )

    errors: list[str] = []
    for attempt_name, command in attempts:
        result = run_command(command)
        if result.returncode == 0:
            return extract_embedding(json.loads(result.stdout))
        message = result.stderr.strip() or result.stdout.strip() or "embedding request failed"
        errors.append(f"{attempt_name}: {message}")

    raise RuntimeError("; ".join(errors))
