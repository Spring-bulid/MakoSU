#!/usr/bin/env bash
set -euo pipefail

REAL_CC="${KSU_REAL_CC:-clang}"
LEGACY_FLAG='-enable-trivial-auto-var-init-zero-knowing-it-will-be-removed-from-clang'
ARGS=()

for arg in "$@"; do
    if [[ "$arg" != "$LEGACY_FLAG" ]]; then
        ARGS+=("$arg")
    fi
done

exec "$REAL_CC" "${ARGS[@]}"
