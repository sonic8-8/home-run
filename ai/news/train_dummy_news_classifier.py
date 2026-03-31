#!/usr/bin/env python3
"""Train and evaluate a dummy-news label classifier from cached embeddings."""

from __future__ import annotations

import argparse
import json
import math
import pickle
import random
import sys
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any, Callable

from news_classifier_utils import ALLOWED_LABELS, LABEL_ORDER, load_jsonl_rows, resolve_path, write_jsonl


try:
    from sklearn.linear_model import LogisticRegression  # type: ignore
    from sklearn.svm import LinearSVC  # type: ignore

    SKLEARN_AVAILABLE = True
except Exception:
    LogisticRegression = None  # type: ignore
    LinearSVC = None  # type: ignore
    SKLEARN_AVAILABLE = False


Vector = list[float]
Predictor = Callable[[Vector], str]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Train/evaluate a dummy-news classifier from embedding cache."
    )
    parser.add_argument("--input", default="outputs/dummy_news.jsonl")
    parser.add_argument("--embeddings", default="outputs/dummy_news_embeddings.jsonl")
    parser.add_argument("--report", default="outputs/dummy_news_eval.json")
    parser.add_argument("--predictions", default="outputs/dummy_news_cv_predictions.jsonl")
    parser.add_argument("--model-out", default="outputs/dummy_news_selected_model.json")
    parser.add_argument("--folds", type=int, default=3)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--min-per-label", type=int, default=3)
    return parser.parse_args()


def load_dataset_rows(input_path: Path) -> list[dict[str, Any]]:
    rows = load_jsonl_rows(input_path)
    cleaned: list[dict[str, Any]] = []
    for row in rows:
        label = row.get("final_label")
        if label not in ALLOWED_LABELS:
            raise ValueError(f"invalid or missing final_label: {label}")
        cleaned.append(row)
    if not cleaned:
        raise ValueError("no dataset rows found")
    return cleaned


def load_embedding_map(embedding_path: Path) -> dict[str, list[float]]:
    rows = load_jsonl_rows(embedding_path)
    mapping: dict[str, list[float]] = {}
    for row in rows:
        doc_id = str(row.get("doc_id", ""))
        embedding = row.get("embedding")
        if doc_id and isinstance(embedding, list):
            mapping[doc_id] = [float(value) for value in embedding]
    return mapping


def validate_label_counts(labels: list[str], min_per_label: int) -> None:
    counts = Counter(labels)
    missing = [label for label in LABEL_ORDER if counts.get(label, 0) < min_per_label]
    if missing:
        details = {label: counts.get(label, 0) for label in missing}
        raise ValueError(
            f"dataset does not satisfy min-per-label={min_per_label}: {details}"
        )


def stratified_kfold_indices(labels: list[str], n_splits: int, seed: int) -> list[list[int]]:
    grouped: dict[str, list[int]] = defaultdict(list)
    for idx, label in enumerate(labels):
        grouped[label].append(idx)

    for label, indices in grouped.items():
        if len(indices) < n_splits:
            raise ValueError(
                f"label {label} only has {len(indices)} rows; need >= {n_splits}"
            )

    rng = random.Random(seed)
    folds: list[list[int]] = [[] for _ in range(n_splits)]
    for label in LABEL_ORDER:
        indices = list(grouped.get(label, []))
        rng.shuffle(indices)
        for offset, idx in enumerate(indices):
            folds[offset % n_splits].append(idx)
    for fold in folds:
        fold.sort()
    return folds


def dot(left: Vector, right: Vector) -> float:
    return sum(l * r for l, r in zip(left, right))


def norm(vector: Vector) -> float:
    return math.sqrt(sum(value * value for value in vector))


def cosine_similarity(left: Vector, right: Vector) -> float:
    left_norm = norm(left)
    right_norm = norm(right)
    if left_norm == 0 or right_norm == 0:
        return 0.0
    return dot(left, right) / (left_norm * right_norm)


def build_majority_predictor(train_labels: list[str]) -> Predictor:
    majority = Counter(train_labels).most_common(1)[0][0]

    def predict(_: Vector) -> str:
        return majority

    return predict


def build_knn_predictor(train_vectors: list[Vector], train_labels: list[str], k: int = 3) -> Predictor:
    effective_k = max(1, min(k, len(train_vectors)))

    def predict(vector: Vector) -> str:
        ranked = [
            (cosine_similarity(vector, train_vector), label)
            for train_vector, label in zip(train_vectors, train_labels)
        ]
        ranked.sort(key=lambda item: item[0], reverse=True)
        topk = ranked[:effective_k]
        counts = Counter(label for _, label in topk)
        best_count = max(counts.values())
        tied_labels = [label for label, count in counts.items() if count == best_count]
        if len(tied_labels) == 1:
            return tied_labels[0]
        sim_sums = {
            label: sum(score for score, candidate_label in topk if candidate_label == label)
            for label in tied_labels
        }
        tied_labels.sort(key=lambda label: (-sim_sums[label], LABEL_ORDER.index(label)))
        return tied_labels[0]

    return predict


def build_centroid_predictor(train_vectors: list[Vector], train_labels: list[str]) -> Predictor:
    by_label: dict[str, list[Vector]] = defaultdict(list)
    for vector, label in zip(train_vectors, train_labels):
        by_label[label].append(vector)

    centroids: dict[str, Vector] = {}
    for label, vectors in by_label.items():
        dims = len(vectors[0])
        centroid = [0.0] * dims
        for vector in vectors:
            for idx, value in enumerate(vector):
                centroid[idx] += value
        centroids[label] = [value / len(vectors) for value in centroid]

    def predict(vector: Vector) -> str:
        ranked = [
            (cosine_similarity(vector, centroid), label)
            for label, centroid in centroids.items()
        ]
        ranked.sort(key=lambda item: (-item[0], LABEL_ORDER.index(item[1])))
        return ranked[0][1]

    return predict


def build_sklearn_logreg_predictor(train_vectors: list[Vector], train_labels: list[str]) -> tuple[Predictor, Any]:
    if not SKLEARN_AVAILABLE or LogisticRegression is None:
        raise RuntimeError("scikit-learn is not available")
    model = LogisticRegression(
        class_weight="balanced",
        max_iter=5000,
        multi_class="ovr",
    )
    model.fit(train_vectors, train_labels)

    def predict(vector: Vector) -> str:
        return str(model.predict([vector])[0])

    return predict, model


def build_sklearn_linear_svc_predictor(train_vectors: list[Vector], train_labels: list[str]) -> tuple[Predictor, Any]:
    if not SKLEARN_AVAILABLE or LinearSVC is None:
        raise RuntimeError("scikit-learn is not available")
    model = LinearSVC(class_weight="balanced", max_iter=5000, dual="auto")
    model.fit(train_vectors, train_labels)

    def predict(vector: Vector) -> str:
        return str(model.predict([vector])[0])

    return predict, model


def evaluate_predictions(true_labels: list[str], predicted_labels: list[str]) -> dict[str, Any]:
    confusion: dict[str, dict[str, int]] = {
        true_label: {pred_label: 0 for pred_label in LABEL_ORDER}
        for true_label in LABEL_ORDER
    }
    for true_label, predicted_label in zip(true_labels, predicted_labels):
        confusion[true_label][predicted_label] += 1

    per_label: dict[str, dict[str, float]] = {}
    recalls: list[float] = []
    f1_values: list[float] = []

    for label in LABEL_ORDER:
        tp = confusion[label][label]
        fp = sum(confusion[other][label] for other in LABEL_ORDER if other != label)
        fn = sum(confusion[label][other] for other in LABEL_ORDER if other != label)
        precision = tp / (tp + fp) if (tp + fp) else 0.0
        recall = tp / (tp + fn) if (tp + fn) else 0.0
        f1 = (
            2 * precision * recall / (precision + recall)
            if (precision + recall)
            else 0.0
        )
        per_label[label] = {
            "precision": precision,
            "recall": recall,
            "f1": f1,
            "support": tp + fn,
        }
        recalls.append(recall)
        f1_values.append(f1)

    correct = sum(1 for true_label, pred_label in zip(true_labels, predicted_labels) if true_label == pred_label)
    accuracy = correct / len(true_labels) if true_labels else 0.0
    macro_f1 = sum(f1_values) / len(f1_values)
    balanced_accuracy = sum(recalls) / len(recalls)
    return {
        "accuracy": accuracy,
        "macro_f1": macro_f1,
        "balanced_accuracy": balanced_accuracy,
        "per_label": per_label,
        "confusion_matrix": confusion,
    }


def build_majority_baseline(labels: list[str], folds: list[list[int]]) -> tuple[dict[str, Any], list[dict[str, Any]]]:
    rows: list[dict[str, Any]] = []
    all_true: list[str] = []
    all_pred: list[str] = []
    for fold_idx, test_indices in enumerate(folds):
        train_indices = [idx for idx in range(len(labels)) if idx not in set(test_indices)]
        train_labels = [labels[idx] for idx in train_indices]
        predictor = build_majority_predictor(train_labels)
        for idx in test_indices:
            predicted = predictor([])
            true_label = labels[idx]
            rows.append(
                {
                    "model": "majority",
                    "fold": fold_idx,
                    "index": idx,
                    "true_label": true_label,
                    "predicted_label": predicted,
                }
            )
            all_true.append(true_label)
            all_pred.append(predicted)
    return evaluate_predictions(all_true, all_pred), rows


def cross_validate_model(
    *,
    model_name: str,
    rows: list[dict[str, Any]],
    vectors: list[Vector],
    labels: list[str],
    folds: list[list[int]],
) -> tuple[dict[str, Any], list[dict[str, Any]], Any | None]:
    all_true: list[str] = []
    all_pred: list[str] = []
    prediction_rows: list[dict[str, Any]] = []

    for fold_idx, test_indices in enumerate(folds):
        test_index_set = set(test_indices)
        train_indices = [idx for idx in range(len(rows)) if idx not in test_index_set]
        train_vectors = [vectors[idx] for idx in train_indices]
        train_labels = [labels[idx] for idx in train_indices]

        trained_model = None
        if model_name == "knn_cosine":
            predictor = build_knn_predictor(train_vectors, train_labels)
        elif model_name == "nearest_centroid":
            predictor = build_centroid_predictor(train_vectors, train_labels)
        elif model_name == "sklearn_logreg":
            predictor, trained_model = build_sklearn_logreg_predictor(train_vectors, train_labels)
        elif model_name == "sklearn_linear_svc":
            predictor, trained_model = build_sklearn_linear_svc_predictor(train_vectors, train_labels)
        else:
            raise ValueError(f"unsupported model: {model_name}")

        for idx in test_indices:
            predicted = predictor(vectors[idx])
            true_label = labels[idx]
            prediction_rows.append(
                {
                    "model": model_name,
                    "fold": fold_idx,
                    "doc_id": rows[idx]["doc_id"],
                    "doc_title": rows[idx].get("doc_title"),
                    "true_label": true_label,
                    "predicted_label": predicted,
                }
            )
            all_true.append(true_label)
            all_pred.append(predicted)

    metrics = evaluate_predictions(all_true, all_pred)
    return metrics, prediction_rows, None


def fit_selected_model(
    model_name: str,
    rows: list[dict[str, Any]],
    vectors: list[Vector],
    labels: list[str],
) -> dict[str, Any]:
    if model_name == "majority":
        majority = Counter(labels).most_common(1)[0][0]
        return {"model_type": model_name, "majority_label": majority}
    if model_name == "knn_cosine":
        return {
            "model_type": model_name,
            "k": 3,
            "doc_ids": [row["doc_id"] for row in rows],
            "labels": labels,
            "vectors": vectors,
        }
    if model_name == "nearest_centroid":
        by_label: dict[str, list[Vector]] = defaultdict(list)
        for vector, label in zip(vectors, labels):
            by_label[label].append(vector)
        centroids: dict[str, Vector] = {}
        for label, group in by_label.items():
            dims = len(group[0])
            centroid = [0.0] * dims
            for vector in group:
                for idx, value in enumerate(vector):
                    centroid[idx] += value
            centroids[label] = [value / len(group) for value in centroid]
        return {"model_type": model_name, "centroids": centroids}
    if model_name == "sklearn_logreg":
        predictor, model = build_sklearn_logreg_predictor(vectors, labels)
        _ = predictor
        return {"model_type": model_name, "pickle_model": model}
    if model_name == "sklearn_linear_svc":
        predictor, model = build_sklearn_linear_svc_predictor(vectors, labels)
        _ = predictor
        return {"model_type": model_name, "pickle_model": model}
    raise ValueError(f"unsupported final model: {model_name}")


def main() -> int:
    args = parse_args()
    input_path = resolve_path(args.input)
    embedding_path = resolve_path(args.embeddings)
    report_path = resolve_path(args.report)
    predictions_path = resolve_path(args.predictions)
    model_out_path = resolve_path(args.model_out)

    if not input_path.exists():
        print(f"dataset not found: {input_path}", file=sys.stderr)
        return 1
    if not embedding_path.exists():
        print(f"embedding cache not found: {embedding_path}", file=sys.stderr)
        return 1

    rows = load_dataset_rows(input_path)
    labels = [str(row["final_label"]) for row in rows]
    validate_label_counts(labels, args.min_per_label)

    embedding_map = load_embedding_map(embedding_path)
    missing_embeddings = [row["doc_id"] for row in rows if row["doc_id"] not in embedding_map]
    if missing_embeddings:
        print(
            f"missing embeddings for {len(missing_embeddings)} doc_ids: {missing_embeddings[:10]}",
            file=sys.stderr,
        )
        return 1

    vectors = [embedding_map[row["doc_id"]] for row in rows]
    dims = {len(vector) for vector in vectors}
    if len(dims) != 1:
        print(f"inconsistent embedding dimensions: {sorted(dims)}", file=sys.stderr)
        return 1

    folds = stratified_kfold_indices(labels, args.folds, args.seed)

    model_names = ["knn_cosine", "nearest_centroid"]
    skipped_models: dict[str, str] = {}
    if SKLEARN_AVAILABLE:
        model_names.extend(["sklearn_logreg", "sklearn_linear_svc"])
    else:
        skipped_models["sklearn_logreg"] = "scikit-learn is not installed"
        skipped_models["sklearn_linear_svc"] = "scikit-learn is not installed"

    evaluations: dict[str, Any] = {}
    prediction_rows: list[dict[str, Any]] = []

    baseline_metrics, baseline_predictions = build_majority_baseline(labels, folds)
    evaluations["majority"] = baseline_metrics
    prediction_rows.extend(baseline_predictions)

    for model_name in model_names:
        metrics, model_predictions, _ = cross_validate_model(
            model_name=model_name,
            rows=rows,
            vectors=vectors,
            labels=labels,
            folds=folds,
        )
        evaluations[model_name] = metrics
        prediction_rows.extend(model_predictions)

    ranked_models = sorted(
        (
            (name, metrics["macro_f1"], metrics["balanced_accuracy"])
            for name, metrics in evaluations.items()
            if name != "majority"
        ),
        key=lambda item: (-item[1], -item[2], item[0] != "sklearn_logreg", item[0]),
    )
    selected_model_name = ranked_models[0][0]
    selected_model = fit_selected_model(selected_model_name, rows, vectors, labels)

    model_out_path.parent.mkdir(parents=True, exist_ok=True)
    if selected_model_name.startswith("sklearn_") and "pickle_model" in selected_model:
        pickle_path = model_out_path.with_suffix(".pkl")
        with pickle_path.open("wb") as handle:
            pickle.dump(selected_model["pickle_model"], handle)
        model_metadata = {
            "model_type": selected_model_name,
            "pickle_path": str(pickle_path),
            "label_order": LABEL_ORDER,
            "embedding_dimensions": next(iter(dims)),
        }
        model_out_path.write_text(json.dumps(model_metadata, ensure_ascii=False, indent=2), encoding="utf-8")
    else:
        model_payload = {
            **selected_model,
            "label_order": LABEL_ORDER,
            "embedding_dimensions": next(iter(dims)),
        }
        model_out_path.write_text(json.dumps(model_payload, ensure_ascii=False), encoding="utf-8")

    report_payload = {
        "dataset_size": len(rows),
        "label_counts": Counter(labels),
        "folds": args.folds,
        "seed": args.seed,
        "embedding_dimensions": next(iter(dims)),
        "sklearn_available": SKLEARN_AVAILABLE,
        "models_evaluated": ["majority", *model_names],
        "models_skipped": skipped_models,
        "models": evaluations,
        "selected_model": selected_model_name,
        "baseline_model": "majority",
    }
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(report_payload, ensure_ascii=False, indent=2), encoding="utf-8")
    write_jsonl(predictions_path, prediction_rows)

    print(f"completed dataset={len(rows)} selected_model={selected_model_name}")
    if skipped_models:
        print(f"skipped_models={json.dumps(skipped_models, ensure_ascii=False)}")
    print(f"report={report_path}")
    print(f"predictions={predictions_path}")
    print(f"model={model_out_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
