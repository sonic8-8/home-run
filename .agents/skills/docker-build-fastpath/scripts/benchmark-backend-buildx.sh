#!/usr/bin/env bash
set -euo pipefail

TARGET="${1:-base}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"
DOCKERFILE="${REPO_ROOT}/backend/Dockerfile.buildkit-cache"
CONTEXT="${REPO_ROOT}/backend"

CACHE_ROOT="${HOMERUN_BUILDX_CACHE_DIR:-${HOME}/.cache/homerun/buildx/backend-bench}"
CACHE_DIR="${CACHE_ROOT}/${TARGET}"
TMP_CACHE_DIR="${CACHE_DIR}.new"

cleanup_builder() {
  local builder="$1"
  docker buildx rm -f "${builder}" >/dev/null 2>&1 || true
}

run_build() {
  local label="$1"
  local builder="$2"
  local use_external_cache="$3"

  cleanup_builder "${builder}"
  docker buildx create --name "${builder}" --driver docker-container --use >/dev/null

  local args=(
    docker buildx build
    --progress=plain
    --load
    --target "${TARGET}"
    -f "${DOCKERFILE}"
  )

  if [[ "${use_external_cache}" == "true" && -d "${CACHE_DIR}" ]]; then
    args+=(--cache-from "type=local,src=${CACHE_DIR}")
  fi

  if [[ "${use_external_cache}" == "true" ]]; then
    rm -rf "${TMP_CACHE_DIR}"
    args+=(--cache-to "type=local,dest=${TMP_CACHE_DIR},mode=max")
  fi

  args+=("${CONTEXT}")

  local start_ts end_ts elapsed
  start_ts="$(date +%s)"
  "${args[@]}" >/tmp/"${builder}".log 2>&1
  end_ts="$(date +%s)"
  elapsed="$((end_ts - start_ts))"

  if [[ "${use_external_cache}" == "true" ]]; then
    rm -rf "${CACHE_DIR}"
    mv "${TMP_CACHE_DIR}" "${CACHE_DIR}"
  fi

  cleanup_builder "${builder}"

  echo "${label}=${elapsed}"
}

rm -rf "${CACHE_DIR}" "${TMP_CACHE_DIR}"
mkdir -p "${CACHE_ROOT}"

echo "target=${TARGET}"
echo "context=${CONTEXT}"
echo "dockerfile=${DOCKERFILE}"
echo "cache_dir=${CACHE_DIR}"

BASELINE="$(run_build fresh_no_external_cache "homerun-bench-${TARGET}-nocache" false)"
WARMUP="$(run_build fresh_with_external_cache_warmup "homerun-bench-${TARGET}-warmup" true)"
REUSED="$(run_build fresh_with_external_cache_reused "homerun-bench-${TARGET}-reused" true)"

echo "${BASELINE}"
echo "${WARMUP}"
echo "${REUSED}"

baseline_seconds="${BASELINE##*=}"
warmup_seconds="${WARMUP##*=}"
reused_seconds="${REUSED##*=}"

warmup_saved="$((baseline_seconds - warmup_seconds))"
reused_saved="$((baseline_seconds - reused_seconds))"

echo "warmup_saved_seconds=${warmup_saved}"
echo "reused_saved_seconds=${reused_saved}"
echo "warmup_saved_percent=$((warmup_saved * 100 / baseline_seconds))"
echo "reused_saved_percent=$((reused_saved * 100 / baseline_seconds))"
