#!/usr/bin/env bash
set -euo pipefail

SOURCE_DIR="$(git rev-parse --show-toplevel)"
OUTPUT_DIR="$(realpath -m "${1:-$SOURCE_DIR/out/lkm}")"
DDK_RELEASE="${DDK_RELEASE:-20260313}"
EXPECTED_SIZE="${KSU_EXPECTED_SIZE:-0x0549}"
EXPECTED_HASH="${KSU_EXPECTED_HASH:-7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943}"
MANAGER_PACKAGE="${KSU_MANAGER_PACKAGE:-com.makosu.manager}"

DEFAULT_KMIS=(
    android12-5.10
    android13-5.10
    android13-5.15
    android14-5.15
    android14-6.1
    android15-6.6
    android16-6.12
)

if [[ -n "${KSU_KMIS:-}" ]]; then
    read -r -a KMIS <<< "$KSU_KMIS"
else
    KMIS=("${DEFAULT_KMIS[@]}")
fi

mkdir -p "$OUTPUT_DIR"

for kmi in "${KMIS[@]}"; do
    image="ghcr.io/ylarod/ddk-min:${kmi}-${DDK_RELEASE}"
    echo "==> Building ${kmi} with ${image}"
    docker pull "$image"
    docker run --rm --privileged \
        -e KMI="$kmi" \
        -e KSU_EXPECTED_SIZE="$EXPECTED_SIZE" \
        -e KSU_EXPECTED_HASH="$EXPECTED_HASH" \
        -e KSU_MANAGER_PACKAGE="$MANAGER_PACKAGE" \
        -v "$SOURCE_DIR:/workspace" \
        -v "$OUTPUT_DIR:/out" \
        -w /workspace/kernel \
        "$image" \
        bash -lc '
            set -euo pipefail
            make clean >/dev/null 2>&1 || true
            CONFIG_KSU=m CC=clang make \
                KSU_EXPECTED_SIZE="$KSU_EXPECTED_SIZE" \
                KSU_EXPECTED_HASH="$KSU_EXPECTED_HASH" \
                KSU_MANAGER_PACKAGE="$KSU_MANAGER_PACKAGE"
            llvm-strip -d kernelsu.ko
            cp kernelsu.ko "/out/${KMI}_kernelsu.ko"
        '
done

sha256sum "$OUTPUT_DIR"/*_kernelsu.ko
