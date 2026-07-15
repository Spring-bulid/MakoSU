/* SPDX-License-Identifier: GPL-2.0-only */
#ifndef __KSU_GKI1_IMPORTS_H
#define __KSU_GKI1_IMPORTS_H

#include <linux/types.h>

bool ksu_gki1_imports_init(void);

#ifdef KSU_GKI1_LKM_IMPORTS
/*
 * Android 11 GKI exposes only a small KMI.  The backing pointers are filled
 * from kallsyms during module initialization, before any of these aliases are
 * used.  Include this header after the relevant kernel declarations.
 */
extern void *ksu_gki1_import_uts_sem;
extern void *ksu_gki1_import_selinux_state;
extern void *ksu_gki1_import_mntns_operations;
extern void *ksu_gki1_import_init_pid_ns;
extern void *ksu_gki1_import_selinux_blob_sizes;
extern void *ksu_gki1_import_init_mm;
extern void *ksu_gki1_import_tasklist_lock;
extern void *ksu_gki1_import_tracepoint_srcu;
extern void *ksu_gki1_import___tracepoint_sys_enter;

#define uts_sem (*((typeof(uts_sem) *)ksu_gki1_import_uts_sem))
#define selinux_state (*((typeof(selinux_state) *)ksu_gki1_import_selinux_state))
#define mntns_operations (*((typeof(mntns_operations) *)ksu_gki1_import_mntns_operations))
#define init_pid_ns (*((typeof(init_pid_ns) *)ksu_gki1_import_init_pid_ns))
#define selinux_blob_sizes (*((typeof(selinux_blob_sizes) *)ksu_gki1_import_selinux_blob_sizes))
#define init_mm (*((typeof(init_mm) *)ksu_gki1_import_init_mm))
#define tasklist_lock (*((typeof(tasklist_lock) *)ksu_gki1_import_tasklist_lock))
#define tracepoint_srcu (*((typeof(tracepoint_srcu) *)ksu_gki1_import_tracepoint_srcu))
#define __tracepoint_sys_enter (*((typeof(__tracepoint_sys_enter) *)ksu_gki1_import___tracepoint_sys_enter))
#endif

#endif
