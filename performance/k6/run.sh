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
K6_DOCKER_NETWORK_VALUE="${K6_DOCKER_NETWORK:-}"
CONTAINER_ROOTDIR="/tmp"
CONTAINER_SUMMARY_DIR="/tmp/k6-results"
SCRIPT_PATH="$1"

shift

mkdir -p "$ROOT_DIR/$SUMMARY_DIR"

env_args=(
  -e "BUILD_NUMBER=${BUILD_NUMBER:-local}"
  -e "GIT_COMMIT_SHORT=${GIT_COMMIT_SHORT_VALUE}"
  -e "K6_SUMMARY_DIR=${CONTAINER_SUMMARY_DIR}"
)

while IFS='=' read -r key value; do
  if [[ "$key" == K6_* ]]; then
    if [[ "$key" == "K6_SUMMARY_DIR" || "$key" == "K6_DOCKER_NETWORK" ]]; then
      continue
    fi
    env_args+=(-e "$key=$value")
  fi
done < <(env)

build_container_command() {
  local command="mkdir -p $(quote_shell "$CONTAINER_SUMMARY_DIR") && k6 run"
  local arg

  for arg in "${RUN_ARGS[@]}"; do
    command="${command} $(quote_shell "$arg")"
  done

  command="${command} $(quote_shell "${CONTAINER_ROOTDIR}/${SCRIPT_PATH#./}")"

  while [ "$#" -gt 0 ]; do
    command="${command} $(quote_shell "$1")"
    shift
  done

  printf '%s' "$command"
}

quote_shell() {
  printf '%q' "$1"
}

docker_args=(
  create
  --user "$(id -u):$(id -g)"
  --entrypoint sh
)

if [ -n "$K6_DOCKER_NETWORK_VALUE" ]; then
  docker_args+=(--network "$K6_DOCKER_NETWORK_VALUE")
fi

command_args=(
  -lc
  "$(build_container_command "$@")"
)

container_id="$(docker "${docker_args[@]}" "${env_args[@]}" "$IMAGE" "${command_args[@]}")"
trap 'docker rm -f "$container_id" >/dev/null 2>&1 || true' EXIT

docker cp "$ROOT_DIR/performance" "$container_id:${CONTAINER_ROOTDIR}/"

set +e
docker start -a "$container_id"
exit_code=$?
set -e

docker cp "$container_id:${CONTAINER_SUMMARY_DIR}/." "$ROOT_DIR/$SUMMARY_DIR/" >/dev/null 2>&1 || true

exit "$exit_code"
