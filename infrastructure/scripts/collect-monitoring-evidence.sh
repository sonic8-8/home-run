#!/usr/bin/env bash

set -uo pipefail

ARTIFACT_ROOT="${ARTIFACT_ROOT:-.agent/private/codex/release-readiness/artifacts/monitoring-evidence}"
RUN_ID="${RUN_ID:-$(date +%Y%m%d-%H%M%S)}"
ARTIFACT_DIR="${ARTIFACT_ROOT}/${RUN_ID}"
STATUS_FILE="${ARTIFACT_DIR}/monitoring-evidence-status.txt"
HTTP_CLIENT_CONTAINER="${HTTP_CLIENT_CONTAINER:-homerun-frontend}"
BACKEND_SERVICE_URL="${BACKEND_SERVICE_URL:-http://backend:8080}"
PROMETHEUS_SERVICE_URL="${PROMETHEUS_SERVICE_URL:-http://prometheus:9090}"
GRAFANA_SERVICE_URL="${GRAFANA_SERVICE_URL:-http://grafana:3000}"
GRAFANA_USER="${GRAFANA_USER:-admin}"
GRAFANA_PASSWORD="${GRAFANA_PASSWORD:-admin}"
DEFAULT_PASSWORD="${DEFAULT_PASSWORD:-Password123!}"
SCRAPE_WAIT_SECONDS="${SCRAPE_WAIT_SECONDS:-20}"
BACKEND_DASHBOARD_UID="${BACKEND_DASHBOARD_UID:-backend-overview}"
JVM_DASHBOARD_UID="${JVM_DASHBOARD_UID:-jvm-overview}"

declare -a FAILURES=()

log() {
  printf '%s\n' "$1" | tee -a "$STATUS_FILE"
}

record_failure() {
  FAILURES+=("$1")
  log "FAIL: $1"
}

record_success() {
  log "OK: $1"
}

prepare_artifact_dir() {
  rm -rf "$ARTIFACT_ROOT"
  mkdir -p "$ARTIFACT_DIR"
  : > "$STATUS_FILE"
}

run_container_wget() {
  local output_file="$1"
  shift

  local exit_code=0
  if docker exec "$HTTP_CLIENT_CONTAINER" wget "$@" >"$output_file" 2>"${output_file}.stderr"; then
    exit_code=0
  else
    exit_code=$?
  fi

  printf '%s\n' "$exit_code" > "${output_file}.status"
  return "$exit_code"
}

http_get() {
  local output_file="$1"
  local url="$2"
  run_container_wget "$output_file" -qO- "$url"
}

http_get_with_header() {
  local output_file="$1"
  local header="$2"
  local url="$3"
  run_container_wget "$output_file" -qO- "--header=${header}" "$url"
}

http_post_json() {
  local output_file="$1"
  local url="$2"
  local json_body="$3"
  run_container_wget \
    "$output_file" \
    -qO- \
    '--header=Content-Type: application/json' \
    "--post-data=${json_body}" \
    "$url"
}

http_post_json_with_header() {
  local output_file="$1"
  local header="$2"
  local url="$3"
  local json_body="$4"
  run_container_wget \
    "$output_file" \
    -qO- \
    '--header=Content-Type: application/json' \
    "--header=${header}" \
    "--post-data=${json_body}" \
    "$url"
}

urlencode() {
  local raw="$1"
  local length="${#raw}"
  local encoded=""
  local index
  local character
  local hex

  for ((index = 0; index < length; index++)); do
    character="${raw:index:1}"
    case "$character" in
      [a-zA-Z0-9.~_-])
        encoded+="$character"
        ;;
      ' ')
        encoded+='%20'
        ;;
      *)
        printf -v hex '%%%02X' "'$character"
        encoded+="$hex"
        ;;
    esac
  done

  printf '%s' "$encoded"
}

prometheus_query() {
  local output_file="$1"
  local query="$2"
  local encoded_query

  encoded_query="$(urlencode "$query")"
  http_get "$output_file" "${PROMETHEUS_SERVICE_URL}/api/v1/query?query=${encoded_query}"
}

json_compact() {
  tr -d '\n' < "$1"
}

extract_first_string() {
  local key="$1"
  local file="$2"

  json_compact "$file" | grep -o "\"${key}\":\"[^\"]*\"" | head -n1 | cut -d'"' -f4
}

extract_first_number() {
  local key="$1"
  local file="$2"

  json_compact "$file" | grep -o "\"${key}\":[0-9][0-9]*" | head -n1 | cut -d: -f2
}

file_has_non_empty_result() {
  local file="$1"
  if [ ! -f "$file" ]; then
    return 1
  fi
  ! grep -q '"result":\[\]' "$file"
}

file_contains() {
  local pattern="$1"
  local file="$2"
  [ -f "$file" ] && grep -q "$pattern" "$file"
}

warm_http_metrics() {
  local warmup_log="${ARTIFACT_DIR}/backend-http-codes.txt"
  : > "$warmup_log"

  local attempt
  for attempt in $(seq 1 20); do
    if run_container_wget \
      "${ARTIFACT_DIR}/warm-login-${attempt}.txt" \
      -qO- \
      '--header=Content-Type: application/json' \
      '--post-data={"email":"nobody@example.com","password":"wrong-password"}' \
      "${BACKEND_SERVICE_URL}/api/auth/login"; then
      printf '200\n' >> "$warmup_log"
      continue
    fi
    printf 'non-2xx\n' >> "$warmup_log"
  done
}

collect_base_evidence() {
  docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' > "${ARTIFACT_DIR}/docker-ps.txt"

  if http_get "${ARTIFACT_DIR}/backend-readiness.json" "${BACKEND_SERVICE_URL}/actuator/health/readiness"; then
    record_success "backend readiness collected"
  else
    record_failure "backend readiness collection failed"
  fi

  if http_get "${ARTIFACT_DIR}/prometheus-metrics.txt" "${BACKEND_SERVICE_URL}/actuator/prometheus"; then
    grep -E 'http_server_requests_seconds_bucket|jvm_threads_live_threads|homerun_(turn_commit|settlement_phase)_duration' \
      "${ARTIFACT_DIR}/prometheus-metrics.txt" \
      > "${ARTIFACT_DIR}/prometheus-sample.txt" || true
    record_success "actuator prometheus sample collected"
  else
    record_failure "actuator prometheus collection failed"
  fi

  if http_get "${ARTIFACT_DIR}/prometheus-targets.json" "${PROMETHEUS_SERVICE_URL}/api/v1/targets"; then
    record_success "prometheus targets collected"
  else
    record_failure "prometheus targets collection failed"
  fi

  warm_http_metrics
  sleep "$SCRAPE_WAIT_SECONDS"

  prometheus_query "${ARTIFACT_DIR}/backend_rps.json" \
    'sum(rate(http_server_requests_seconds_count{uri!~"/actuator.*"}[5m]))' || true
  prometheus_query "${ARTIFACT_DIR}/backend_p95_ms.json" \
    'histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{uri!~"/actuator.*"}[5m]))) * 1000' || true
  prometheus_query "${ARTIFACT_DIR}/backend_p99_ms.json" \
    'histogram_quantile(0.99, sum by (le) (rate(http_server_requests_seconds_bucket{uri!~"/actuator.*"}[5m]))) * 1000' || true
  prometheus_query "${ARTIFACT_DIR}/jvm_heap_used.json" \
    'jvm_memory_used_bytes{area="heap"}' || true
  prometheus_query "${ARTIFACT_DIR}/jvm_threads_live.json" \
    'jvm_threads_live_threads' || true
}

create_synthetic_session() {
  local email="rr-monitoring-${RUN_ID}@example.com"
  local signup_json
  local login_json
  local access_token
  local authorization_header
  local region_code
  local district_code
  local property_id
  local session_id

  signup_json="{\"name\":\"RR Gate 3\",\"email\":\"${email}\",\"password\":\"${DEFAULT_PASSWORD}\",\"passwordConfirm\":\"${DEFAULT_PASSWORD}\",\"termsAgreed\":true}"
  login_json="{\"email\":\"${email}\",\"password\":\"${DEFAULT_PASSWORD}\"}"

  if ! http_post_json "${ARTIFACT_DIR}/signup.json" "${BACKEND_SERVICE_URL}/api/auth/signup" "$signup_json"; then
    record_failure "synthetic signup failed"
    return 1
  fi

  if ! http_post_json "${ARTIFACT_DIR}/login.json" "${BACKEND_SERVICE_URL}/api/auth/login" "$login_json"; then
    record_failure "synthetic login failed"
    return 1
  fi

  access_token="$(extract_first_string accessToken "${ARTIFACT_DIR}/login.json")"
  if [ -z "$access_token" ]; then
    record_failure "access token extraction failed"
    return 1
  fi
  authorization_header="Authorization: Bearer ${access_token}"

  if ! http_get_with_header \
    "${ARTIFACT_DIR}/regions.json" \
    "${authorization_header}" \
    "${BACKEND_SERVICE_URL}/api/games/regions"; then
    record_failure "region list collection failed"
    return 1
  fi

  region_code="$(extract_first_string regionCode "${ARTIFACT_DIR}/regions.json")"
  if [ -z "$region_code" ]; then
    record_failure "region code extraction failed"
    return 1
  fi

  if ! http_get_with_header \
    "${ARTIFACT_DIR}/districts.json" \
    "${authorization_header}" \
    "${BACKEND_SERVICE_URL}/api/games/regions/${region_code}/districts"; then
    record_failure "district list collection failed"
    return 1
  fi

  district_code="$(extract_first_string districtCode "${ARTIFACT_DIR}/districts.json")"
  if [ -z "$district_code" ]; then
    record_failure "district code extraction failed"
    return 1
  fi

  if ! http_get_with_header \
    "${ARTIFACT_DIR}/properties.json" \
    "${authorization_header}" \
    "${BACKEND_SERVICE_URL}/api/games/regions/${region_code}/districts/${district_code}/properties"; then
    record_failure "target property list collection failed"
    return 1
  fi

  property_id="$(extract_first_number propertyId "${ARTIFACT_DIR}/properties.json")"
  if [ -z "$property_id" ]; then
    record_failure "target property id extraction failed"
    return 1
  fi

  if ! http_post_json_with_header \
    "${ARTIFACT_DIR}/create-session.json" \
    "${authorization_header}" \
    "${BACKEND_SERVICE_URL}/api/games/sessions" \
    "{\"slotNumber\":1,\"characterType\":\"FEMALE\",\"characterName\":\"RR Gate 3\",\"jobType\":\"STARTUP\",\"regionCode\":\"${region_code}\",\"districtCode\":\"${district_code}\",\"targetPropertyId\":${property_id},\"useMyData\":false}"; then
    record_failure "game session creation failed"
    return 1
  fi

  session_id="$(extract_first_number sessionId "${ARTIFACT_DIR}/create-session.json")"
  if [ -z "$session_id" ]; then
    record_failure "session id extraction failed"
    return 1
  fi

  if ! http_post_json_with_header \
    "${ARTIFACT_DIR}/turn-slots.json" \
    "${authorization_header}" \
    "${BACKEND_SERVICE_URL}/api/games/sessions/${session_id}/turn/slots" \
    '{"slots":[{"slotIndex":0,"actionType":"REST"},{"slotIndex":1,"actionType":"REST"},{"slotIndex":2,"actionType":"REST"}]}'; then
    record_failure "turn slot submission failed"
    return 1
  fi

  if ! http_post_json_with_header \
    "${ARTIFACT_DIR}/turn-commit.json" \
    "${authorization_header}" \
    "${BACKEND_SERVICE_URL}/api/games/sessions/${session_id}/turn/commit" \
    '{}'; then
    record_failure "turn commit failed"
    return 1
  fi

  record_success "synthetic session created and committed"
  return 0
}

collect_business_metric_evidence() {
  sleep "$SCRAPE_WAIT_SECONDS"

  http_get "${ARTIFACT_DIR}/prometheus-metrics-after-turn.txt" "${BACKEND_SERVICE_URL}/actuator/prometheus" || true

  prometheus_query "${ARTIFACT_DIR}/turn-commit-metric.json" \
    'sum by (boundary, result) (homerun_turn_commit_duration_seconds_count)' || true
  prometheus_query "${ARTIFACT_DIR}/settlement-phase-metric.json" \
    'sum by (phase) (homerun_settlement_phase_duration_seconds_count)' || true

  if file_contains 'homerun_turn_commit_duration_seconds' "${ARTIFACT_DIR}/prometheus-metrics-after-turn.txt"; then
    grep 'homerun_turn_commit_duration_seconds' "${ARTIFACT_DIR}/prometheus-metrics-after-turn.txt" \
      > "${ARTIFACT_DIR}/turn-commit-sample.txt" || true
  fi

  if file_contains 'homerun_settlement_phase_duration_seconds' "${ARTIFACT_DIR}/prometheus-metrics-after-turn.txt"; then
    grep 'homerun_settlement_phase_duration_seconds' "${ARTIFACT_DIR}/prometheus-metrics-after-turn.txt" \
      > "${ARTIFACT_DIR}/settlement-phase-sample.txt" || true
  fi
}

collect_grafana_exports() {
  local basic_auth

  basic_auth="$(printf '%s:%s' "$GRAFANA_USER" "$GRAFANA_PASSWORD" | base64 | tr -d '\n')"

  if http_get_with_header \
    "${ARTIFACT_DIR}/backend-overview-export.json" \
    "Authorization: Basic ${basic_auth}" \
    "${GRAFANA_SERVICE_URL}/api/dashboards/uid/${BACKEND_DASHBOARD_UID}"; then
    record_success "backend dashboard export collected"
  else
    record_failure "backend dashboard export failed"
  fi

  if http_get_with_header \
    "${ARTIFACT_DIR}/jvm-overview-export.json" \
    "Authorization: Basic ${basic_auth}" \
    "${GRAFANA_SERVICE_URL}/api/dashboards/uid/${JVM_DASHBOARD_UID}"; then
    record_success "jvm dashboard export collected"
  else
    record_failure "jvm dashboard export failed"
  fi
}

validate_required_artifacts() {
  file_has_non_empty_result "${ARTIFACT_DIR}/backend_p95_ms.json" \
    && record_success "backend p95 latency rendered" \
    || record_failure "backend p95 latency result is empty"

  file_has_non_empty_result "${ARTIFACT_DIR}/backend_p99_ms.json" \
    && record_success "backend p99 latency rendered" \
    || record_failure "backend p99 latency result is empty"

  file_has_non_empty_result "${ARTIFACT_DIR}/jvm_threads_live.json" \
    && record_success "jvm thread metric rendered" \
    || record_failure "jvm thread metric result is empty"

  file_has_non_empty_result "${ARTIFACT_DIR}/turn-commit-metric.json" \
    && record_success "turn commit metric recorded" \
    || record_failure "turn commit metric result is empty"

  file_has_non_empty_result "${ARTIFACT_DIR}/settlement-phase-metric.json" \
    && record_success "settlement phase metric recorded" \
    || record_failure "settlement phase metric result is empty"

  file_contains '"dashboard"' "${ARTIFACT_DIR}/backend-overview-export.json" \
    && record_success "backend dashboard export is valid" \
    || record_failure "backend dashboard export is missing"

  file_contains '"dashboard"' "${ARTIFACT_DIR}/jvm-overview-export.json" \
    && record_success "jvm dashboard export is valid" \
    || record_failure "jvm dashboard export is missing"
}

write_manifest() {
  find "$ARTIFACT_DIR" -maxdepth 1 -type f | sort > "${ARTIFACT_DIR}/artifact-manifest.txt"
}

main() {
  prepare_artifact_dir
  collect_base_evidence
  create_synthetic_session || true
  collect_business_metric_evidence
  collect_grafana_exports || true
  validate_required_artifacts
  write_manifest

  if [ "${#FAILURES[@]}" -gt 0 ]; then
    log "SUMMARY: Monitoring evidence incomplete"
    printf '%s\n' "${FAILURES[@]}" > "${ARTIFACT_DIR}/failures.txt"
    exit 1
  fi

  log "SUMMARY: Monitoring evidence ready"
}

main "$@"
