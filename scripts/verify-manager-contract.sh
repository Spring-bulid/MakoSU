#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
kbuild="$root_dir/kernel/Kbuild"
gradle_properties="$root_dir/manager/gradle.properties"
manager_build="$root_dir/manager/app/build.gradle.kts"
ksud_build="$root_dir/userspace/ksud/build.rs"

kernel_package="$(sed -n 's/^KSU_MANAGER_PACKAGE := //p' "$kbuild")"
gradle_package="$(sed -n 's/^KSU_PACKAGE_NAME=//p' "$gradle_properties")"

test -n "$kernel_package"
test -n "$gradle_package"
test "$kernel_package" = "$gradle_package"
grep -q "else \"$kernel_package\"" "$manager_build"
grep -q "unwrap_or_else(|_| \"$kernel_package\".to_string())" "$ksud_build"
grep -Eq '^KSU_EXPECTED_SIZE := 0x[0-9a-fA-F]+$' "$kbuild"
grep -Eq '^KSU_EXPECTED_HASH := [0-9a-f]{64}$' "$kbuild"

printf 'MakoSU manager identity contract verified for %s\n' "$kernel_package"
