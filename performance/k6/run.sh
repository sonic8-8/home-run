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
RUN_ARGS=()

if [ -n "${K6_PROMETHEUS_RW_SERVER_URL:-}" ] && [ -z "${K6_PROMETHEUS_RW_TREND_STATS:-}" ]; then
  export K6_PROMETHEUS_RW_TREND_STATS="p(90),p(95),avg,max"
fi

if [ -n "${K6_PROMETHEUS_RW_SERVER_URL:-}" ]; then
  RUN_ARGS+=(-o experimental-prometheus-rw)
fi

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
  run "${RUN_ARGS[@]}" "$@"
