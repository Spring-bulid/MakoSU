#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 4 ]]; then
    echo "Usage: $0 <kernel-source.tar.gz> <kernel-headers.tar.gz> <Module.symvers> <output-dir>" >&2
    exit 2
fi

SOURCE_ARCHIVE="$(realpath "$1")"
HEADERS_ARCHIVE="$(realpath "$2")"
MODULE_SYMVERS="$(realpath "$3")"
OUTPUT_DIR="$4"

for archive in "$SOURCE_ARCHIVE" "$HEADERS_ARCHIVE"; do
    tar -tzf "$archive" >/dev/null
done

mkdir -p "$OUTPUT_DIR"
OUTPUT_DIR="$(realpath "$OUTPUT_DIR")"
if find "$OUTPUT_DIR" -mindepth 1 -print -quit | grep -q .; then
    echo "Output directory must be empty: $OUTPUT_DIR" >&2
    exit 1
fi

tar -xzf "$SOURCE_ARCHIVE" -C "$OUTPUT_DIR" --strip-components=1
tar -xzf "$HEADERS_ARCHIVE" -C "$OUTPUT_DIR" --transform='s#^kernel-headers//*##'
install -m 0644 "$MODULE_SYMVERS" "$OUTPUT_DIR/Module.symvers"

AUTOCONF="$OUTPUT_DIR/include/generated/autoconf.h"
if [[ ! -f "$AUTOCONF" ]]; then
    echo "Generated autoconf.h is missing from the headers archive" >&2
    exit 1
fi

awk '
    /^#define CONFIG_/ {
        name = $2
        $1 = ""
        $2 = ""
        sub(/^[[:space:]]+/, "")
        value = $0

        if (name ~ /_MODULE$/ && value == "1") {
            name = substr(name, 1, length(name) - 7)
            value = "m"
        } else if (value == "1") {
            value = "y"
        }

        print name "=" value
    }
' "$AUTOCONF" >"$OUTPUT_DIR/.config"

mkdir -p "$OUTPUT_DIR/include/config"
cp "$OUTPUT_DIR/.config" "$OUTPUT_DIR/include/config/auto.conf"
grep '=m$' "$OUTPUT_DIR/.config" >"$OUTPUT_DIR/include/config/tristate.conf" || true
: >"$OUTPUT_DIR/include/config/auto.conf.cmd"

VERSION="$(sed -nE 's/^VERSION[[:space:]]*=[[:space:]]*//p' "$OUTPUT_DIR/Makefile" | head -n 1)"
PATCHLEVEL="$(sed -nE 's/^PATCHLEVEL[[:space:]]*=[[:space:]]*//p' "$OUTPUT_DIR/Makefile" | head -n 1)"
UTS_RELEASE="$(sed -nE 's/^#define UTS_RELEASE "([^"]+)"/\1/p' "$OUTPUT_DIR/include/generated/utsrelease.h")"

if [[ "$VERSION.$PATCHLEVEL" != "5.4" || "$UTS_RELEASE" != 5.4.*-android11-* ]]; then
    echo "Prepared tree is not an Android 11 5.4 GKI tree: version=$VERSION.$PATCHLEVEL release=$UTS_RELEASE" >&2
    exit 1
fi

printf 'Prepared Android 11 5.4 tree at %s\n' "$OUTPUT_DIR"
printf 'Kernel release: %s\n' "$UTS_RELEASE"
