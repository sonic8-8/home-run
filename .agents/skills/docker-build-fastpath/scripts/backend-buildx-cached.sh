#!/usr/bin/env bash
set -euo pipefail

TARGET="${1:-tester}"
TAG="${2:-}"
BUILDER="${HOMERUN_BUILDX_BUILDER:-homerun-backend-cache}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"
DOCKERFILE="${REPO_ROOT}/backend/Dockerfile.buildkit-cache"
CONTEXT="${REPO_ROOT}/backend"

CACHE_ROOT="${HOMERUN_BUILDX_CACHE_DIR:-${HOME}/.cache/homerun/buildx/backend}"
CACHE_DIR="${CACHE_ROOT}/${TARGET}"
TMP_CACHE_DIR="${CACHE_DIR}.new"

ensure_builder() {
  if ! docker buildx inspect "${BUILDER}" >/dev/null 2>&1; then
    docker buildx create --name "${BUILDER}" --driver docker-container >/dev/null
  fi

  docker buildx inspect "${BUILDER}" --bootstrap >/dev/null
}

mkdir -p "${CACHE_ROOT}"
rm -rf "${TMP_CACHE_DIR}"

ensure_builder

args=(
  docker buildx build
  --builder "${BUILDER}"
  --progress="${BUILDX_PROGRESS:-plain}"
  --load
  --target "${TARGET}"
  -f "${DOCKERFILE}"
)

if [[ -n "${TAG}" ]]; then
  args+=(-t "${TAG}")
fi

if [[ -d "${CACHE_DIR}" ]]; then
  args+=(--cache-from "type=local,src=${CACHE_DIR}")
fi

args+=(--cache-to "type=local,dest=${TMP_CACHE_DIR},mode=max")
args+=("${CONTEXT}")

echo "repo_root=${REPO_ROOT}"
echo "target=${TARGET}"
echo "cache_dir=${CACHE_DIR}"
if [[ -n "${TAG}" ]]; then
  echo "tag=${TAG}"
fi
printf 'command='
printf '%q ' "${args[@]}"
printf '\n'

start_ts="$(date +%s)"
"${args[@]}"
end_ts="$(date +%s)"

rm -rf "${CACHE_DIR}"
mv "${TMP_CACHE_DIR}" "${CACHE_DIR}"

echo "builder=${BUILDER}"
echo "elapsed_seconds=$((end_ts - start_ts))"
echo "cache_saved=${CACHE_DIR}"
