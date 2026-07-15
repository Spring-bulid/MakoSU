#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel)"
MODULE_DIR="$ROOT_DIR/kernel"
KERNEL_DIR="${KSU_GKI1_KDIR:-${1:-}}"
OUTPUT_DIR="$(realpath -m "${2:-$ROOT_DIR/out/lkm-gki1}")"
TARGET="${KSU_GKI1_TARGET:-android11-5.4}"
EXPECTED_SIZE="${KSU_EXPECTED_SIZE:-0x0549}"
EXPECTED_HASH="${KSU_EXPECTED_HASH:-7eb729e2d76e05488cc4150825e69be9a8beca33bf606ea9217e163eea3b3943}"
MANAGER_PACKAGE="${KSU_MANAGER_PACKAGE:-com.makosu.manager}"
CC_BIN="${KSU_CC:-clang}"
LEGACY_AUTO_VAR_INIT_FLAG='-enable-trivial-auto-var-init-zero-knowing-it-will-be-removed-from-clang'

if [[ -z "$KERNEL_DIR" ]]; then
    echo "Usage: $0 <prepared-android11-5.4-kernel-dir> [output-dir]" >&2
    exit 2
fi

KERNEL_DIR="$(realpath "$KERNEL_DIR")"

for file in Makefile .config Module.symvers include/generated/autoconf.h; do
    if [[ ! -f "$KERNEL_DIR/$file" ]]; then
        echo "Missing prepared kernel file: $KERNEL_DIR/$file" >&2
        exit 1
    fi
done

read_make_value() {
    sed -nE "s/^$1[[:space:]]*=[[:space:]]*//p" "$KERNEL_DIR/Makefile" | head -n 1
}

require_config() {
    if ! grep -qx "$1=y" "$KERNEL_DIR/.config"; then
        echo "Required kernel config is not enabled: $1=y" >&2
        exit 1
    fi
}

VERSION="$(read_make_value VERSION)"
PATCHLEVEL="$(read_make_value PATCHLEVEL)"
if [[ "$VERSION" != "5" || "$PATCHLEVEL" != "4" ]]; then
    echo "GKI 1.0 LKM requires a 5.4 kernel tree, found $VERSION.$PATCHLEVEL" >&2
    exit 1
fi

if [[ "$TARGET" != "android11-5.4" ]]; then
    echo "Unsupported GKI 1.0 target: $TARGET" >&2
    exit 1
fi

require_config CONFIG_ARM64
require_config CONFIG_MODULES
require_config CONFIG_KPROBES
require_config CONFIG_KALLSYMS
require_config CONFIG_TRACEPOINTS

SIGN_KEY="${KSU_MODULE_SIGN_KEY:-}"
SIGN_CERT="${KSU_MODULE_SIGN_CERT:-}"
if [[ -n "$SIGN_KEY" || -n "$SIGN_CERT" ]]; then
    if [[ -z "$SIGN_KEY" || -z "$SIGN_CERT" ]]; then
        echo "KSU_MODULE_SIGN_KEY and KSU_MODULE_SIGN_CERT must be set together" >&2
        exit 1
    fi
elif grep -qx 'CONFIG_MODULE_SIG_FORCE=y' "$KERNEL_DIR/.config"; then
    echo "This kernel enforces module signatures; provide KSU_MODULE_SIGN_KEY and KSU_MODULE_SIGN_CERT" >&2
    exit 1
fi

mkdir -p "$OUTPUT_DIR"

if ! printf '' | "$CC_BIN" -x c -c -o /dev/null "$LEGACY_AUTO_VAR_INIT_FLAG" - 2>/dev/null; then
    export KSU_REAL_CC="$CC_BIN"
    CC_BIN="bash $ROOT_DIR/scripts/clang-gki1-wrapper.sh"
    echo "Compiler does not support the Android 11 legacy auto-var-init gate; using compatibility wrapper"
fi

KERNEL_MAKE_ARGS=(
    -C "$KERNEL_DIR"
    ARCH=arm64
    CROSS_COMPILE="${KSU_CROSS_COMPILE:-aarch64-linux-gnu-}"
    CLANG_TRIPLE="${KSU_CLANG_TRIPLE:-aarch64-linux-gnu-}"
    LLVM_IAS=1
    CC="$CC_BIN"
    HOSTCC="${KSU_HOSTCC:-clang}"
    LD="${KSU_LD:-ld.lld}"
    HOSTLD="${KSU_HOSTLD:-ld.lld}"
    AR="${KSU_AR:-llvm-ar}"
    NM="${KSU_NM:-llvm-nm}"
    OBJCOPY="${KSU_OBJCOPY:-llvm-objcopy}"
    OBJDUMP="${KSU_OBJDUMP:-llvm-objdump}"
    STRIP="${KSU_LLVM_STRIP:-llvm-strip}"
)

MAKE_ARGS=(
    "${KERNEL_MAKE_ARGS[@]}"
    M="$MODULE_DIR"
    src="$MODULE_DIR"
    CONFIG_KSU=m
    KSU_EXPECTED_SIZE="$EXPECTED_SIZE"
    KSU_EXPECTED_HASH="$EXPECTED_HASH"
    KSU_MANAGER_PACKAGE="$MANAGER_PACKAGE"
)

make "${MAKE_ARGS[@]}" clean
make "${KERNEL_MAKE_ARGS[@]}" scripts_basic
if [[ ! -x "$KERNEL_DIR/scripts/mod/modpost" ]]; then
    UTS_RELEASE="$(sed -nE 's/^#define UTS_RELEASE "([^"]+)"/\1/p' "$KERNEL_DIR/include/generated/utsrelease.h")"
    make "${KERNEL_MAKE_ARGS[@]}" CONFIG_SYSTEM_TRUSTED_KEYRING= prepare0
    printf '%s\n' "$UTS_RELEASE" >"$KERNEL_DIR/include/config/kernel.release"
    printf '#define UTS_RELEASE "%s"\n' "$UTS_RELEASE" >"$KERNEL_DIR/include/generated/utsrelease.h"
fi
make "${MAKE_ARGS[@]}" -j"${KSU_BUILD_JOBS:-$(nproc)}" modules

OUTPUT_MODULE="$OUTPUT_DIR/${TARGET}_kernelsu.ko"
cp "$MODULE_DIR/kernelsu.ko" "$OUTPUT_MODULE"
"${KSU_LLVM_STRIP:-llvm-strip}" --strip-debug "$OUTPUT_MODULE"

verify_kmi_imports() {
    local symbol
    local failed=0

    while IFS= read -r symbol; do
        if ! awk -v symbol="$symbol" '$2 == symbol { found = 1; exit } END { exit !found }' "$KERNEL_DIR/Module.symvers"; then
            echo "Module references a non-KMI symbol: $symbol" >&2
            failed=1
        fi
    done < <("${KSU_LLVM_NM:-llvm-nm}" -u "$OUTPUT_MODULE" | awk '$1 == "U" { print $2 }' | sort -u)

    if ((failed)); then
        echo "GKI 1.0 KMI import verification failed" >&2
        exit 1
    fi
}

verify_kmi_imports

if [[ -n "$SIGN_KEY" ]]; then
    "$KERNEL_DIR/scripts/sign-file" sha256 "$SIGN_KEY" "$SIGN_CERT" "$OUTPUT_MODULE"
fi

if command -v modinfo >/dev/null 2>&1; then
    modinfo -F vermagic "$OUTPUT_MODULE"
fi
sha256sum "$OUTPUT_MODULE"
