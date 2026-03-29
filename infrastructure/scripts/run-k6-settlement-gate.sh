#!/usr/bin/env bash

set -euo pipefail

export K6_BASE_URL="${K6_BASE_URL:-http://backend:8080}"
export K6_DOCKER_NETWORK="${K6_DOCKER_NETWORK:-homerun-runtime_web_net}"
export K6_SUMMARY_DIR="${K6_SUMMARY_DIR:-.agent/private/codex/release-readiness/artifacts/release-evidence/k6/settlement-gate}"
export K6_SETTLEMENT_COMMIT_P95_MS="${K6_SETTLEMENT_COMMIT_P95_MS:-2000}"
export K6_VUS="${K6_VUS:-1}"
export K6_ITERATIONS="${K6_ITERATIONS:-1}"

bash performance/k6/run.sh performance/k6/settlement-commit.js
