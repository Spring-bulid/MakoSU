/* SPDX-License-Identifier: GPL-2.0-only */
#include <linux/errno.h>
#include <linux/init.h>
#include <linux/kallsyms.h>
#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/sizes.h>
#include <linux/string.h>

#include "infra/gki1_imports.h"

#ifdef KSU_GKI1_LKM_IMPORTS

#define KSU_GKI1_IMPORTS(F)                                                                                           \
    F(static_key_enable)                                                                                                \
    F(static_key_count)                                                                                                 \
    F(change_pid)                                                                                                       \
    F(avtab_destroy)                                                                                                    \
    F(avtab_alloc)                                                                                                      \
    F(avtab_insert_nonunique)                                                                                           \
    F(avtab_search_node_next)                                                                                           \
    F(avtab_search_node)                                                                                                \
    F(ebitmap_get_bit)                                                                                                  \
    F(ebitmap_set_bit)                                                                                                  \
    F(hashtab_insert)                                                                                                   \
    F(hashtab_search)                                                                                                   \
    F(selinux_status_update_policyload)                                                                                 \
    F(selnl_notify_policyload)                                                                                          \
    F(avc_ss_reset)                                                                                                     \
    F(security_release_secctx)                                                                                          \
    F(security_secid_to_secctx)                                                                                         \
    F(security_secctx_to_secid)                                                                                         \
    F(unregister_kprobe)                                                                                                 \
    F(register_kprobe)                                                                                                   \
    F(ext4_unregister_sysfs)                                                                                            \
    F(put_seccomp_filter)                                                                                               \
    F(abort_creds)                                                                                                       \
    F(commit_creds)                                                                                                      \
    F(free_uid)                                                                                                          \
    F(alloc_uid)                                                                                                         \
    F(set_groups)                                                                                                        \
    F(groups_sort)                                                                                                       \
    F(groups_alloc)                                                                                                      \
    F(groups_free)                                                                                                       \
    F(task_work_add)                                                                                                     \
    F(fsnotify_destroy_mark)                                                                                            \
    F(fsnotify_add_mark)                                                                                                \
    F(fsnotify_put_mark)                                                                                                \
    F(fsnotify_init_mark)                                                                                               \
    F(fsnotify_put_group)                                                                                               \
    F(fsnotify_alloc_group)                                                                                             \
    F(kallsyms_lookup)                                                                                                  \
    F(kallsyms_lookup_name)                                                                                             \
    F(kallsyms_lookup_size_offset)                                                                                      \
    F(ksys_unshare)                                                                                                     \
    F(__arm64_sys_setns)                                                                                                \
    F(set_fs_pwd)                                                                                                       \
    F(ns_get_path)                                                                                                       \
    F(find_pid_ns)                                                                                                       \
    F(alloc_anon_inode)                                                                                                 \
    F(alloc_file_pseudo)                                                                                                \
    F(__flush_dcache_area)                                                                                              \
    F(__set_fixmap)                                                                                                      \
    F(stop_machine)                                                                                                      \
    F(find_task_by_vpid)                                                                                                \
    F(get_task_cred)                                                                                                     \
    F(unregister_kretprobe)                                                                                              \
    F(register_kretprobe)                                                                                                \
    F(static_key_disable)                                                                                               \
    F(prepare_creds)                                                                                                     \
    F(uts_sem)                                                                                                           \
    F(selinux_state)                                                                                                     \
    F(mntns_operations)                                                                                                  \
    F(init_pid_ns)                                                                                                       \
    F(selinux_blob_sizes)                                                                                                \
    F(init_mm)                                                                                                           \
    F(tasklist_lock)                                                                                                     \
    F(tracepoint_srcu)                                                                                                   \
    F(__tracepoint_sys_enter)

#define KSU_GKI1_DECLARE_SLOT(symbol) void *ksu_gki1_import_##symbol __visible __used;
KSU_GKI1_IMPORTS(KSU_GKI1_DECLARE_SLOT)

struct ksu_gki1_import {
    const char *name;
    void **slot;
};

#define KSU_GKI1_IMPORT_ENTRY(symbol) { #symbol, &ksu_gki1_import_##symbol },
static struct ksu_gki1_import ksu_gki1_imports[] = { KSU_GKI1_IMPORTS(KSU_GKI1_IMPORT_ENTRY) };

static unsigned long (*ksu_gki1_kallsyms_lookup_name)(const char *name);

static unsigned long __nocfi ksu_gki1_lookup_name(const char *name)
{
    return ksu_gki1_kallsyms_lookup_name(name);
}

static unsigned long ksu_gki1_bootstrap_kallsyms_lookup_name(void)
{
    const unsigned long sprint = (unsigned long)sprint_symbol;
    const char target[] = "kallsyms_lookup_name";
    char name[KSYM_SYMBOL_LEN];
    unsigned long offset;

    /*
     * sprint_symbol is part of the Android 11 KMI.  The lookup helper lives
     * close to it in the core text on every supported 5.4 GKI build; search in
     * instruction-sized increments so KASLR does not affect the bootstrap.
     */
    for (offset = 0; offset <= SZ_512K; offset += sizeof(u32)) {
        unsigned long candidate = sprint - offset;

        sprint_symbol(name, candidate);
        if (!strncmp(name, target, sizeof(target) - 1) && name[sizeof(target) - 1] == '+')
            return candidate;

        if (!offset)
            continue;

        candidate = sprint + offset;
        sprint_symbol(name, candidate);
        if (!strncmp(name, target, sizeof(target) - 1) && name[sizeof(target) - 1] == '+')
            return candidate;
    }

    return 0;
}

bool __init ksu_gki1_imports_init(void)
{
    size_t i;

    if (ksu_gki1_kallsyms_lookup_name)
        return true;

    ksu_gki1_kallsyms_lookup_name =
        (void *)ksu_gki1_bootstrap_kallsyms_lookup_name();
    if (!ksu_gki1_kallsyms_lookup_name) {
        pr_err("gki1: unable to bootstrap kallsyms_lookup_name\n");
        return false;
    }

    for (i = 0; i < ARRAY_SIZE(ksu_gki1_imports); i++) {
        unsigned long address;

        if (!strcmp(ksu_gki1_imports[i].name, "kallsyms_lookup_name"))
            address = (unsigned long)ksu_gki1_kallsyms_lookup_name;
        else
            address = ksu_gki1_lookup_name(ksu_gki1_imports[i].name);

        if (!address) {
            pr_err("gki1: required symbol is unavailable: %s\n", ksu_gki1_imports[i].name);
            return false;
        }
        *ksu_gki1_imports[i].slot = (void *)address;
    }

    pr_info("gki1: imported %zu non-KMI symbols\n", ARRAY_SIZE(ksu_gki1_imports));
    return true;
}

#else

bool __init ksu_gki1_imports_init(void)
{
    return true;
}

#endif
