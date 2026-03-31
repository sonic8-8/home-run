#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from langchain_community.retrievers import BM25Retriever
from langchain_core.documents import Document
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import RunnableLambda
from langchain_huggingface import HuggingFacePipeline
from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline


PROJECT_DIR = Path(__file__).resolve().parent
EULGU_SOURCE_PATH = PROJECT_DIR / "eulgu_quiz_samples.json"
RIGHTS_SOURCE_PATH = PROJECT_DIR.parent / "V1" / "json" / "registry_rights_samples.json"

REQUIRED_TOP_LEVEL_KEYS = [
    "id",
    "pattern",
    "pattern_label",
    "type_label",
    "quiz_verdict",
    "eulgu_rows",
    "issue_summary",
    "key_points",
    "feedback_correct",
    "feedback_wrong",
]

REQUIRED_ROW_KEYS = ["rank_no", "purpose", "receipt", "reason", "details"]


@dataclass(frozen=True)
class RowSpec:
    rank_no: str
    purpose: str
    token_ratios: dict[str, int]


@dataclass(frozen=True)
class PatternSpec:
    pattern: str
    pattern_label: str
    type_label: str
    quiz_verdict: str
    rows: tuple[RowSpec, ...]
    summary_hint: str


EULGU_PATTERN_SPECS: dict[str, PatternSpec] = {
    "clean_eulgu": PatternSpec(
        pattern="clean_eulgu",
        pattern_label="권리 없음 정상형",
        type_label="정상",
        quiz_verdict="정상",
        rows=(),
        summary_hint="현재 유효한 을구 권리가 없는 가장 기본적인 정상형",
    ),
    "low_ratio_single_mortgage": PatternSpec(
        pattern="low_ratio_single_mortgage",
        pattern_label="저비율 근저당권 단독형",
        type_label="정상",
        quiz_verdict="정상",
        rows=(RowSpec(rank_no="1", purpose="근저당권설정", token_ratios={"max_claim_amount": 15}),),
        summary_hint="근저당권은 있으나 매매가 대비 부담 비율이 매우 낮은 정상형에 가까운 구조",
    ),
    "high_ratio_single_mortgage": PatternSpec(
        pattern="high_ratio_single_mortgage",
        pattern_label="고비율 근저당권 단독형",
        type_label="을구이상만",
        quiz_verdict="위험",
        rows=(RowSpec(rank_no="1", purpose="근저당권설정", token_ratios={"max_claim_amount": 90}),),
        summary_hint="근저당권 1건이지만 채권최고액 비율이 매우 높은 위험 구조",
    ),
    "multiple_mortgages": PatternSpec(
        pattern="multiple_mortgages",
        pattern_label="복수 근저당권 병존형",
        type_label="을구이상만",
        quiz_verdict="위험",
        rows=(
            RowSpec(rank_no="1", purpose="근저당권설정", token_ratios={"first_max_claim_amount": 50}),
            RowSpec(rank_no="2", purpose="근저당권설정", token_ratios={"second_max_claim_amount": 30}),
        ),
        summary_hint="선순위와 후순위 근저당권이 함께 남아 있는 복합 부담 구조",
    ),
    "active_leasehold": PatternSpec(
        pattern="active_leasehold",
        pattern_label="전세권 현재 존속형",
        type_label="을구이상만",
        quiz_verdict="위험",
        rows=(RowSpec(rank_no="1", purpose="전세권설정", token_ratios={"jeonse_amount": 70}),),
        summary_hint="현재 유효한 전세권이 남아 있는 위험 구조",
    ),
    "active_tenant_right": PatternSpec(
        pattern="active_tenant_right",
        pattern_label="임차권등기 현재 존속형",
        type_label="을구이상만",
        quiz_verdict="위험",
        rows=(RowSpec(rank_no="1", purpose="임차권등기", token_ratios={"deposit_amount": 60}),),
        summary_hint="현재 임차권등기가 남아 있어 보증금 반환 부담이 의심되는 구조",
    ),
    "mortgage_plus_tenant_right": PatternSpec(
        pattern="mortgage_plus_tenant_right",
        pattern_label="근저당권과 임차권 병존형",
        type_label="을구이상만",
        quiz_verdict="위험",
        rows=(
            RowSpec(rank_no="1", purpose="근저당권설정", token_ratios={"max_claim_amount": 35}),
            RowSpec(rank_no="2", purpose="임차권등기", token_ratios={"deposit_amount": 45}),
        ),
        summary_hint="담보권과 임차 관련 권리가 동시에 남아 있는 복합 위험 구조",
    ),
}


def load_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def build_documents_from_eulgu_samples(data: dict[str, Any]) -> list[Document]:
    documents: list[Document] = []
    for sample in data.get("samples", []):
        pattern = sample["pattern"]
        verdict = sample["quiz_verdict"]

        for row in sample.get("eulgu_rows", []):
            purpose = row.get("purpose", "")
            row_text = (
                f"패턴 {pattern} / 을구 / 목적 {purpose} / 접수 {row.get('receipt', '')} / "
                f"원인 {row.get('reason', '')} / 상세 {row.get('details', '')}"
            )
            documents.append(
                Document(
                    page_content=row_text,
                    metadata={
                        "doc_type": "row_example",
                        "section": "eulgu",
                        "pattern": pattern,
                        "purpose": purpose,
                        "quiz_verdict": verdict,
                        "source": "eulgu_quiz_samples",
                    },
                )
            )

            term_text = (
                f"등기 목적 {purpose}\n"
                f"등기 원인: {row.get('reason', '')}\n"
                f"권리자 및 기타사항: {row.get('details', '')}"
            )
            documents.append(
                Document(
                    page_content=term_text,
                    metadata={
                        "doc_type": "term",
                        "section": "eulgu",
                        "pattern": pattern,
                        "purpose": purpose,
                        "quiz_verdict": verdict,
                        "source": "eulgu_quiz_samples",
                    },
                )
            )

        style_fields = {
            "issue_summary": sample.get("issue_summary", ""),
            "feedback_correct": sample.get("feedback_correct", ""),
            "feedback_wrong": sample.get("feedback_wrong", ""),
        }
        for field_name, text in style_fields.items():
            if not text:
                continue
            documents.append(
                Document(
                    page_content=text,
                    metadata={
                        "doc_type": "style",
                        "field": field_name,
                        "section": "eulgu",
                        "pattern": pattern,
                        "quiz_verdict": verdict,
                        "source": "eulgu_quiz_samples",
                    },
                )
            )

        for point in sample.get("key_points", []):
            documents.append(
                Document(
                    page_content=point,
                    metadata={
                        "doc_type": "style",
                        "field": "key_point",
                        "section": "eulgu",
                        "pattern": pattern,
                        "quiz_verdict": verdict,
                        "source": "eulgu_quiz_samples",
                    },
                )
            )
    return documents


def build_documents_from_registry_rights(data: dict[str, Any]) -> list[Document]:
    documents: list[Document] = []
    for sample in data.get("samples", []):
        verdict = sample.get("quiz_verdict", "위험")
        type_code = sample.get("type_code", "")
        pattern = sample.get("type_code", "")
        for row in sample.get("eulgu_rows", []):
            purpose = row.get("purpose", "")
            row_text = (
                f"패턴 {pattern} / 을구 / 목적 {purpose} / "
                f"원인 {row.get('reason', '')} / 상세 {row.get('details', '')}"
            )
            documents.append(
                Document(
                    page_content=row_text,
                    metadata={
                        "doc_type": "row_example",
                        "section": "eulgu",
                        "pattern": pattern,
                        "purpose": purpose,
                        "quiz_verdict": verdict,
                        "source": "registry_rights_samples",
                        "type_code": type_code,
                    },
                )
            )

        if sample.get("issue_summary"):
            documents.append(
                Document(
                    page_content=sample["issue_summary"],
                    metadata={
                        "doc_type": "style",
                        "field": "issue_summary",
                        "section": "eulgu",
                        "pattern": pattern,
                        "quiz_verdict": verdict,
                        "source": "registry_rights_samples",
                        "type_code": type_code,
                    },
                )
            )
    return documents


def load_corpus() -> list[Document]:
    eulgu_data = load_json(EULGU_SOURCE_PATH)
    rights_data = load_json(RIGHTS_SOURCE_PATH)
    return build_documents_from_eulgu_samples(eulgu_data) + build_documents_from_registry_rights(rights_data)


def filter_documents(documents: list[Document], *, pattern: str, purpose: str | None, doc_type: str) -> list[Document]:
    filtered = [
        doc
        for doc in documents
        if doc.metadata.get("section") == "eulgu" and doc.metadata.get("doc_type") == doc_type
    ]
    pattern_filtered = [doc for doc in filtered if doc.metadata.get("pattern") == pattern]
    if pattern_filtered:
        filtered = pattern_filtered

    if purpose:
        purpose_filtered = [doc for doc in filtered if doc.metadata.get("purpose") == purpose]
        if purpose_filtered:
            filtered = purpose_filtered

    return filtered


def bm25_top_k(documents: list[Document], query: str, k: int) -> list[Document]:
    if not documents:
        return []
    retriever = BM25Retriever.from_documents(documents)
    retriever.k = min(k, len(documents))
    return retriever.invoke(query)


def retrieve_context(documents: list[Document], spec: PatternSpec) -> dict[str, list[Document]]:
    purposes = [row.purpose for row in spec.rows]
    purpose_hint = " ".join(purposes) if purposes else "권리 없음"
    verdict_hint = spec.quiz_verdict
    base_query = f"을구 {spec.pattern} {spec.pattern_label} {purpose_hint} {verdict_hint}"

    term_hits: list[Document] = []
    row_hits: list[Document] = []
    for purpose in purposes[:2]:
        term_hits.extend(bm25_top_k(filter_documents(documents, pattern=spec.pattern, purpose=purpose, doc_type="term"), base_query, 1))
        row_hits.extend(
            bm25_top_k(filter_documents(documents, pattern=spec.pattern, purpose=purpose, doc_type="row_example"), base_query, 1)
        )

    if not term_hits:
        term_hits = bm25_top_k(filter_documents(documents, pattern=spec.pattern, purpose=None, doc_type="term"), base_query, 2)
    if not row_hits:
        row_hits = bm25_top_k(filter_documents(documents, pattern=spec.pattern, purpose=None, doc_type="row_example"), base_query, 2)

    style_hits = bm25_top_k(filter_documents(documents, pattern=spec.pattern, purpose=None, doc_type="style"), base_query, 3)

    return {
        "term": dedupe_documents(term_hits)[:2],
        "row_example": dedupe_documents(row_hits)[:2],
        "style": dedupe_documents(style_hits)[:3],
    }


def dedupe_documents(documents: list[Document]) -> list[Document]:
    seen: set[tuple[str, tuple[tuple[str, Any], ...]]] = set()
    result: list[Document] = []
    for doc in documents:
        key = (doc.page_content, tuple(sorted(doc.metadata.items())))
        if key in seen:
            continue
        seen.add(key)
        result.append(doc)
    return result


def serialize_docs(docs_by_type: dict[str, list[Document]]) -> list[dict[str, Any]]:
    serialized: list[dict[str, Any]] = []
    for doc_type, docs in docs_by_type.items():
        for doc in docs:
            serialized.append(
                {
                    "type": doc_type,
                    "text": doc.page_content,
                    "metadata": {
                        key: value
                        for key, value in doc.metadata.items()
                        if key in {"pattern", "purpose", "quiz_verdict", "field", "source"}
                    },
                }
            )
    return serialized


def build_request_context(pattern: str, sale_price: int, corpus: list[Document]) -> dict[str, Any]:
    if pattern not in EULGU_PATTERN_SPECS:
        supported = ", ".join(sorted(EULGU_PATTERN_SPECS))
        raise ValueError(f"지원하지 않는 pattern입니다: {pattern}. 지원 목록: {supported}")

    spec = EULGU_PATTERN_SPECS[pattern]
    retrieved = retrieve_context(corpus, spec)
    planner_rows = [
        {
            "rank_no": row.rank_no,
            "purpose": row.purpose,
            "token_ratios": row.token_ratios,
        }
        for row in spec.rows
    ]

    return {
        "task_meta": {
            "task": "render_registry_quiz",
            "section": "eulgu",
            "pattern": spec.pattern,
            "pattern_label": spec.pattern_label,
            "quiz_verdict": spec.quiz_verdict,
        },
        "listing_meta": {
            "sale_price": sale_price,
            "money_rounding_unit": "만원",
            "render_on_output": False,
        },
        "planner_rows": planner_rows,
        "summary_hint": spec.summary_hint,
        "retrieved_context": serialize_docs(retrieved),
        "output_contract": {
            "required_top_level_keys": REQUIRED_TOP_LEVEL_KEYS,
            "required_row_keys": REQUIRED_ROW_KEYS,
            "notes": [
                "반드시 JSON object만 출력할 것",
                "markdown fence를 쓰지 말 것",
                "eulgu_rows는 rank_no 오름차순으로 작성할 것",
                "details 안에 있는 {token_name} placeholder와 rendering key를 일치시킬 것",
                "금액성 placeholder가 없으면 rendering을 생략해도 됨",
            ],
        },
    }


def build_text_generation_pipeline(model_id: str) -> tuple[AutoTokenizer, HuggingFacePipeline]:
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(model_id, torch_dtype="auto", device_map="auto")
    generation_pipeline = pipeline(
        "text-generation",
        model=model,
        tokenizer=tokenizer,
        max_new_tokens=1400,
        do_sample=False,
        return_full_text=False,
    )
    llm = HuggingFacePipeline(pipeline=generation_pipeline)
    return tokenizer, llm


def format_chat_prompt(tokenizer: AutoTokenizer, payload: dict[str, Any]) -> str:
    system_text = (
        "너는 등기부등본 을구 퀴즈 JSON 생성기다. "
        "반드시 유효한 JSON object만 출력해야 하며, 설명문과 row 문체는 실제 등기부 스타일을 최대한 유지해야 한다."
    )
    user_text = json.dumps(payload, ensure_ascii=False, indent=2)
    messages = [
        {"role": "system", "content": system_text},
        {"role": "user", "content": user_text},
    ]

    if hasattr(tokenizer, "apply_chat_template") and getattr(tokenizer, "chat_template", None):
        return tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)

    return f"System:\n{system_text}\n\nUser:\n{user_text}\n\nAssistant:\n"


def extract_json_text(raw: str) -> str:
    cleaned = raw.strip()
    if cleaned.startswith("```"):
        cleaned = re.sub(r"^```(?:json)?\s*", "", cleaned)
        cleaned = re.sub(r"\s*```$", "", cleaned)

    decoder = json.JSONDecoder()
    start = cleaned.find("{")
    while start != -1:
        try:
            _, end = decoder.raw_decode(cleaned[start:])
            return cleaned[start : start + end]
        except json.JSONDecodeError:
            start = cleaned.find("{", start + 1)
    raise ValueError("LLM 출력에서 JSON object를 찾지 못했습니다.")


def normalize_sample(sample: dict[str, Any], spec: PatternSpec) -> dict[str, Any]:
    normalized = dict(sample)
    normalized.setdefault("id", f"generated_{spec.pattern}")
    normalized["pattern"] = spec.pattern
    normalized["pattern_label"] = spec.pattern_label
    normalized["type_label"] = spec.type_label
    normalized["quiz_verdict"] = spec.quiz_verdict

    key_points = normalized.get("key_points", [])
    if isinstance(key_points, str):
        normalized["key_points"] = [line.strip("- ").strip() for line in key_points.splitlines() if line.strip()]

    rows = normalized.get("eulgu_rows", [])
    if spec.pattern == "clean_eulgu" and not isinstance(rows, list):
        normalized["eulgu_rows"] = []
    return normalized


def validate_sample(sample: dict[str, Any], spec: PatternSpec) -> None:
    missing = [key for key in REQUIRED_TOP_LEVEL_KEYS if key not in sample]
    if missing:
        raise ValueError(f"필수 top-level key 누락: {missing}")

    if not isinstance(sample["eulgu_rows"], list):
        raise ValueError("eulgu_rows는 list여야 합니다.")

    if len(sample["eulgu_rows"]) != len(spec.rows):
        raise ValueError(f"eulgu_rows 개수가 pattern spec과 다릅니다. expected={len(spec.rows)} actual={len(sample['eulgu_rows'])}")

    for row, row_spec in zip(sample["eulgu_rows"], spec.rows):
        row_missing = [key for key in REQUIRED_ROW_KEYS if key not in row]
        if row_missing:
            raise ValueError(f"row 필수 key 누락: {row_missing}")
        if str(row["rank_no"]) != row_spec.rank_no:
            raise ValueError(f"rank_no 불일치: expected={row_spec.rank_no} actual={row['rank_no']}")
        if row["purpose"] != row_spec.purpose:
            raise ValueError(f"purpose 불일치: expected={row_spec.purpose} actual={row['purpose']}")

        details = row["details"]
        rendering = row.get("rendering", {})
        expected_tokens = set(row_spec.token_ratios)
        found_tokens = set(re.findall(r"\{([^{}]+)\}", details))
        if found_tokens != expected_tokens:
            raise ValueError(f"details placeholder 불일치: expected={sorted(expected_tokens)} actual={sorted(found_tokens)}")

        if expected_tokens and not isinstance(rendering, dict):
            raise ValueError("금액 placeholder가 있는 row는 rendering dict가 필요합니다.")

        for token_name, ratio_percent in row_spec.token_ratios.items():
            if token_name not in rendering:
                raise ValueError(f"rendering key 누락: {token_name}")
            token_meta = rendering[token_name]
            if token_meta.get("source") != "sale_price_ratio":
                raise ValueError(f"rendering.source는 sale_price_ratio여야 합니다: {token_name}")
            if int(token_meta.get("ratio_percent", -1)) != ratio_percent:
                raise ValueError(
                    f"rendering.ratio_percent 불일치: token={token_name} expected={ratio_percent} actual={token_meta.get('ratio_percent')}"
                )
            if token_meta.get("rounding_unit") != "만원":
                raise ValueError(f"rendering.rounding_unit은 만원이어야 합니다: {token_name}")


def build_chain(tokenizer: AutoTokenizer, llm: HuggingFacePipeline, corpus: list[Document]):
    return (
        RunnableLambda(lambda inputs: build_request_context(inputs["pattern"], inputs["sale_price"], corpus))
        | RunnableLambda(lambda payload: format_chat_prompt(tokenizer, payload))
        | llm
        | StrOutputParser()
    )


def write_output(path: Path, sample: dict[str, Any]) -> None:
    path.write_text(json.dumps(sample, ensure_ascii=False, indent=2), encoding="utf-8")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="LangChain 기반 을구 퀴즈 샘플 생성 프로토타입")
    parser.add_argument("--pattern", required=True, choices=sorted(EULGU_PATTERN_SPECS))
    parser.add_argument("--sale-price", type=int, default=48000, help="매매가 기준 금액. 단위는 만원.")
    parser.add_argument("--model-id", default="Qwen/Qwen2.5-3B-Instruct")
    parser.add_argument("--output", type=Path, help="결과 JSON 저장 경로")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    corpus = load_corpus()
    tokenizer, llm = build_text_generation_pipeline(args.model_id)
    chain = build_chain(tokenizer, llm, corpus)

    raw_output = chain.invoke({"pattern": args.pattern, "sale_price": args.sale_price})
    json_text = extract_json_text(raw_output)
    sample = json.loads(json_text)
    spec = EULGU_PATTERN_SPECS[args.pattern]
    normalized = normalize_sample(sample, spec)
    validate_sample(normalized, spec)

    if args.output:
        write_output(args.output, normalized)
        print(f"saved: {args.output}")
    else:
        print(json.dumps(normalized, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
