#ifndef __KSU_H_DYNAMIC_MANAGER
#define __KSU_H_DYNAMIC_MANAGER

#include <linux/types.h>
#include "uapi/supercall.h"

// MakoSU has no manager/manager_sign.h; keep the sign key type here.
typedef struct {
    unsigned size;
    const char *sha256;
} apk_sign_key_t;

struct dynamic_manager_config {
    unsigned size;
    char hash[65];
    int is_set;
};

// Dynamic sign operations
int ksu_handle_dynamic_manager(struct ksu_dynamic_manager_cmd *cmd);
bool ksu_is_dynamic_manager_enabled(void);
apk_sign_key_t ksu_get_dynamic_manager_sign(void);

#endif
