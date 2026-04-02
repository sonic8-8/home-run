#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"
BACKEND_DIR="${REPO_ROOT}/backend"

docker run --rm \
  -v "${BACKEND_DIR}:/workspace" \
  alpine:3.20 \
  sh -lc 'rm -rf /workspace/build/* /workspace/build/.[!.]* /workspace/build/..?* 2>/dev/null || true'

echo "cleaned=${BACKEND_DIR}/build"
