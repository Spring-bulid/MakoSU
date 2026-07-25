//
// Created by weishu on 2022/12/9.
//

#ifndef KERNELSU_KSU_H
#define KERNELSU_KSU_H

#include <cstdint>
#include <sys/ioctl.h>
#include <sys/prctl.h>
#include <utility>

#include "uapi/ksu.h"

uint32_t get_kernel_uapi_version();

uint32_t get_manager_uapi_version();

uint32_t get_version();

bool uid_should_umount(int uid);

bool is_safe_mode();

bool is_lkm_mode();

bool is_late_load_mode();

bool is_manager();

bool is_pr_build();

using p_key_t = char[KSU_MAX_PACKAGE_NAME];

bool set_app_profile(const app_profile *profile);

int get_app_profile(app_profile *profile);

// Su compat
bool set_su_enabled(bool enabled);

bool is_su_enabled();

// Kernel umount
bool set_kernel_umount_enabled(bool enabled);

bool is_kernel_umount_enabled();

// SELinux hide
int set_selinux_hide_enabled(bool enabled);

bool is_selinux_hide_enabled();

bool get_allow_list(struct ksu_new_get_allow_list_cmd *);

bool get_full_version(char* buff);
bool get_hook_type(char *buff);

// Dynamic manager list (supercall 'K', 106). The canonical uapi definitions
// (ksu_get_managers_cmd / KSU_IOCTL_GET_MANAGERS) land in uapi/supercall.h
// together with the kernel side; these local copies keep the manager
// buildable independently until then.
struct ksu_dm_manager_entry {
    uint32_t uid;
    uint8_t signature_index;
} __attribute__((packed));

struct ksu_dm_get_managers_cmd {
    uint16_t count; // Input / Output: number of managers in array
    uint16_t total_count; // Output: total number of managers in requested list
    struct ksu_dm_manager_entry managers[]; // Output: array of active managers
} __attribute__((packed));

// Get active manager list. Allocates *out_cmd with malloc on success, the
// caller must free() it. Returns false (and sets *out_cmd to nullptr) when
// the feature is unavailable.
bool get_managers_list(struct ksu_dm_get_managers_cmd **out_cmd);

inline std::pair<int, int> legacy_get_info() {
    int32_t version = -1;
    int32_t flags = 0;
    int32_t result = 0;
    prctl(static_cast<int>(0xDEADBEEF), 2, &version, &flags, &result);
    return {version, flags};
}

#define DEFINE_CACHED_GETTER(name, ioctl, cmd_type, field, size) \
    static char g_##name[size] = {0}; \
    bool get_##name(char *buff) { \
        if (g_##name[0] == '\0') { \
            struct cmd_type cmd = {0}; \
            if (ksuctl(ioctl, &cmd) == 0) { \
                snprintf(g_##name, sizeof(g_##name), "%s", cmd.field); \
            } \
        } \
        if (g_##name[0] != '\0') { \
            snprintf(buff, size, "%s", g_##name); \
            return true; \
        } \
        return false; \
    }

#endif //KERNELSU_KSU_H
