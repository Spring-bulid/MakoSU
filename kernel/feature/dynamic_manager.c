#include <linux/err.h>
#include <linux/kernel.h>
#include <linux/string.h>

#include "klog.h" // IWYU pragma: keep
#include "manager/apk_sign.h"
#include "manager/manager_identity.h"
#include "manager/throne_tracker.h"
#include "feature/dynamic_manager.h"

// Dynamic sign configuration
static struct dynamic_manager_config dynamic_manager = {
    .size = 0x300,
    .hash = "0000000000000000000000000000000000000000000000000000000000000000",
    .is_set = 0
};

bool ksu_is_dynamic_manager_enabled(void)
{
    return dynamic_manager.is_set;
}

apk_sign_key_t ksu_get_dynamic_manager_sign(void)
{
    apk_sign_key_t sign_key = { .size = dynamic_manager.size, .sha256 = dynamic_manager.hash };

    return sign_key;
}

// Multi-manager model: only drop entries crowned via the dynamic signature;
// static-signed managers keep their throne.
static void ksu_dynamic_manager_unseat(void)
{
    ksu_remove_manager_by_signature_index(KSU_SIGNATURE_INDEX_DYNAMIC_MANAGER);
}

int ksu_handle_dynamic_manager(struct ksu_dynamic_manager_cmd *cmd)
{
    int ret = 0;
    int i;

    if (!cmd) {
        return -EINVAL;
    }

    switch (cmd->operation) {
    case DYNAMIC_MANAGER_OP_SET_SYNCHRONOUS:
    case DYNAMIC_MANAGER_OP_SET:
        if (cmd->size < 0x100 || cmd->size > 0x1000) {
            pr_err("invalid size: 0x%x\n", cmd->size);
            return -EINVAL;
        }

        // Validate hash format
        for (i = 0; i < 64; i++) {
            char c = cmd->hash[i];
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f'))) {
                pr_err("invalid hash character at position %d: %c\n", i, c);
                return -EINVAL;
            }
        }

        // Replacing an old dynamic sign: unseat whoever it crowned.
        ksu_dynamic_manager_unseat();

        dynamic_manager.size = cmd->size;
        // userspace always put an char[64] to our
        // we just use memcpy to copy memory, and flag [64] to \0 by ourselves
        memcpy(dynamic_manager.hash, cmd->hash, 64);
        dynamic_manager.hash[64] = '\0';

        dynamic_manager.is_set = 1;

        // MakoSU's track_throne() is always synchronous, so SET and
        // SET_SYNCHRONOUS behave the same. Static-signed managers keep their
        // throne; the search additionally crowns apks matching the new
        // dynamic sign (multi-manager model).
        track_throne(false);
        pr_info("dynamic manager updated: size=0x%x, hash=%.16s\n", cmd->size, cmd->hash);
        break;

    case DYNAMIC_MANAGER_OP_GET:
        if (dynamic_manager.is_set) {
            cmd->size = dynamic_manager.size;

            // only copy [64] is enough, userspace will handle that
            memcpy(cmd->hash, dynamic_manager.hash, 64);
            ret = 0;
        } else {
            ret = -ENODATA;
        }
        break;
    case DYNAMIC_MANAGER_OP_WIPE:
        dynamic_manager.is_set = 0;
        ret = 0;
        // Unseat the dynamic-signed manager, then re-crown a static-signed
        // one if present.
        ksu_dynamic_manager_unseat();
        track_throne(false);
        pr_info("dynamic manager kernel settings reseted\n");
        break;

    default:
        pr_err("Invalid dynamic manager operation: %d\n", cmd->operation);
        return -EINVAL;
    }

    return ret;
}
