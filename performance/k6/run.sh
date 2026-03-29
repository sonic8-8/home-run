#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "usage: bash performance/k6/run.sh <script> [k6 args...]" >&2
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
IMAGE="${K6_DOCKER_IMAGE:-grafana/k6:0.49.0}"
SUMMARY_DIR="${K6_SUMMARY_DIR:-performance/k6/results}"
GIT_COMMIT_SHORT_VALUE="${GIT_COMMIT_SHORT:-$(git -C "$ROOT_DIR" rev-parse --short HEAD)}"

mkdir -p "$ROOT_DIR/$SUMMARY_DIR"

env_args=(
  -e "BUILD_NUMBER=${BUILD_NUMBER:-local}"
  -e "GIT_COMMIT_SHORT=${GIT_COMMIT_SHORT_VALUE}"
  -e "K6_SUMMARY_DIR=${SUMMARY_DIR}"
)

while IFS='=' read -r key value; do
  if [[ "$key" == K6_* ]]; then
    env_args+=(-e "$key=$value")
  fi
done < <(env)

docker run --rm -i \
  -u "$(id -u):$(id -g)" \
  "${env_args[@]}" \
  -v "$ROOT_DIR:/work" \
  -w /work \
  "$IMAGE" \
  run "$@"
