#ifndef __KSU_H_SUSFS3S
#define __KSU_H_SUSFS3S

#include <linux/compiler.h>
#include <linux/types.h>

struct ksu_susfs3s_cmd;

// supercall backend for KSU_IOCTL_SUSFS3S ('K', 107). Manages the sus_path
// and sus_kstat lists. Callers are gated by manager_or_root in dispatch.c.
int ksu_handle_susfs3s_cmd(struct ksu_susfs3s_cmd *cmd);

// sus_path: returns true when the *at syscall carrying this path must fail
// with -ENOENT for the current caller. uid 0 and crowned managers are exempt
// so that privileged tooling can still see the real filesystem.
bool ksu_susfs3s_path_hidden(int dfd, const char __user *pathname);

// sus_kstat: overlay the spoofed stat template onto a successful newfstatat
// result. No-op when the path has no registered template.
void ksu_susfs3s_try_spoof_kstat(int dfd, const char __user *pathname, void __user *statbuf);

#endif // __KSU_H_SUSFS3S
