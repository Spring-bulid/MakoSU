#!/system/bin/sh
set -u

section() {
    printf '\n[%s]\n' "$1"
}

section identity
uname -a
cat /proc/version 2>/dev/null || true
printf 'android_release=%s\n' "$(getprop ro.build.version.release 2>/dev/null)"
printf 'sdk=%s\n' "$(getprop ro.build.version.sdk 2>/dev/null)"
printf 'device=%s\n' "$(getprop ro.product.device 2>/dev/null)"
printf 'slot_suffix=%s\n' "$(getprop ro.boot.slot_suffix 2>/dev/null)"
printf 'verified_boot=%s\n' "$(getprop ro.boot.verifiedbootstate 2>/dev/null)"
getenforce 2>/dev/null || true

section kernel_config
if [ -r /proc/config.gz ]; then
    zcat /proc/config.gz 2>/dev/null | grep -E '^(CONFIG_(ARM64|MODULES|MODVERSIONS|MODULE_SIG|MODULE_SIG_FORCE|KPROBES|KALLSYMS|KALLSYMS_ALL|TRACEPOINTS|FTRACE|EXT4_FS)=|# CONFIG_(MODULE_SIG_FORCE|MODVERSIONS) is not set)'
else
    printf '/proc/config.gz is unavailable\n'
fi

section boot_partitions
for partition in boot init_boot vendor_boot; do
    for suffix in "" _a _b; do
        path="/dev/block/by-name/${partition}${suffix}"
        if [ -e "$path" ]; then
            ls -l "$path"
        fi
    done
done

section module_vermagic
found_module=false
for directory in /vendor/lib/modules /vendor_dlkm/lib/modules /odm/lib/modules; do
    [ -d "$directory" ] || continue
    for module in "$directory"/*.ko; do
        [ -f "$module" ] || continue
        printf 'module=%s\n' "$module"
        if command -v modinfo >/dev/null 2>&1; then
            modinfo "$module" 2>/dev/null | grep -E '^(filename|version|vermagic|signer|sig_key|sig_hashalgo):' || true
        else
            strings "$module" 2>/dev/null | grep -m 1 '^vermagic=' || true
        fi
        found_module=true
        break 2
    done
done

if [ "$found_module" = false ]; then
    printf 'No vendor kernel module found\n'
fi
