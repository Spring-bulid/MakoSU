#include <linux/err.h>
#include <linux/fs.h>
#include <linux/list.h>
#include <linux/slab.h>
#include <linux/string.h>
#include <linux/types.h>
#include <linux/uaccess.h>
#include <linux/version.h>

#include "policy/allowlist.h"
#include "manager/apk_sign.h"
#include "klog.h" // IWYU pragma: keep
#include "manager/manager_identity.h"
#include "manager/throne_tracker.h"
#include "feature/dynamic_manager.h"
#include "uapi/supercall.h"

// Crowned-manager list: small fixed-capacity table, see manager_identity.h.
struct ksu_manager_slot {
    uid_t appid;
    u8 signature_index;
    bool valid;
};

static struct ksu_manager_slot ksu_manager_list[KSU_MAX_MANAGERS];
static DEFINE_SPINLOCK(ksu_manager_list_lock);

bool ksu_is_manager_appid_valid(void)
{
    unsigned long flags;
    bool valid = false;
    int i;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (ksu_manager_list[i].valid) {
            valid = true;
            break;
        }
    }
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
    return valid;
}

bool is_uid_manager(uid_t uid)
{
    unsigned long flags;
    bool found = false;
    uid_t appid = uid % KSU_PER_USER_RANGE;
    int i;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (ksu_manager_list[i].valid && ksu_manager_list[i].appid == appid) {
            found = true;
            break;
        }
    }
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
    return found;
}

bool is_manager(void)
{
    return is_uid_manager(current_uid().val);
}

uid_t ksu_get_manager_appid(void)
{
    unsigned long flags;
    uid_t appid = KSU_INVALID_APPID;
    int i;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (ksu_manager_list[i].valid) {
            appid = ksu_manager_list[i].appid;
            break;
        }
    }
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
    return appid;
}

bool ksu_add_manager_appid(uid_t appid, u8 signature_index)
{
    unsigned long flags;
    int i;
    int free_slot = -1;

    appid %= KSU_PER_USER_RANGE;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (!ksu_manager_list[i].valid) {
            if (free_slot < 0)
                free_slot = i;
            continue;
        }
        if (ksu_manager_list[i].appid == appid) {
            // already crowned; refresh the signature index
            ksu_manager_list[i].signature_index = signature_index;
            spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
            return true;
        }
    }
    if (free_slot < 0) {
        spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
        pr_warn("manager list is full (%d), cannot crown uid=%d\n", KSU_MAX_MANAGERS, appid);
        return false;
    }
    ksu_manager_list[free_slot].appid = appid;
    ksu_manager_list[free_slot].signature_index = signature_index;
    ksu_manager_list[free_slot].valid = true;
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
    return true;
}

bool ksu_remove_manager_appid(uid_t appid)
{
    unsigned long flags;
    bool found = false;
    int i;

    appid %= KSU_PER_USER_RANGE;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (ksu_manager_list[i].valid && ksu_manager_list[i].appid == appid) {
            ksu_manager_list[i].valid = false;
            found = true;
        }
    }
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
    return found;
}

void ksu_remove_manager_by_signature_index(u8 signature_index)
{
    unsigned long flags;
    int i;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (ksu_manager_list[i].valid && ksu_manager_list[i].signature_index == signature_index)
            ksu_manager_list[i].valid = false;
    }
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
}

bool ksu_has_manager_signature_index(u8 signature_index)
{
    unsigned long flags;
    bool found = false;
    int i;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (ksu_manager_list[i].valid && ksu_manager_list[i].signature_index == signature_index) {
            found = true;
            break;
        }
    }
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
    return found;
}

void ksu_invalidate_manager_uid(void)
{
    unsigned long flags;
    int i;

    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++)
        ksu_manager_list[i].valid = false;
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
}

#define SYSTEM_PACKAGES_LIST_PATH "/data/system/packages.list"

struct uid_data {
    struct list_head list;
    u32 uid;
    char package[KSU_MAX_PACKAGE_NAME];
};

static void crown_manager(const char *apk, struct list_head *uid_data, u8 signature_index)
{
    char pkg[KSU_MAX_PACKAGE_NAME];
    if (get_pkg_from_apk_path(pkg, apk) < 0) {
        pr_err("Failed to get package name from apk path: %s\n", apk);
        return;
    }

    pr_info("manager pkg: %s\n", pkg);

    struct list_head *list = (struct list_head *)uid_data;
    struct uid_data *np;

    list_for_each_entry (np, list, list) {
        if (strncmp(np->package, pkg, KSU_MAX_PACKAGE_NAME) == 0) {
            pr_info("Crowning manager: %s(uid=%d, signature_index=%d)\n", pkg, np->uid, signature_index);
            ksu_add_manager_appid(np->uid, signature_index);
            break;
        }
    }
}

#define DATA_PATH_LEN 384 // 384 is enough for /data/app/<package>/base.apk

struct data_path {
    char dirpath[DATA_PATH_LEN];
    int depth;
    struct list_head list;
};

struct apk_path_hash {
    unsigned int hash;
    bool exists;
    struct list_head list;
};

static struct list_head apk_path_hash_list = LIST_HEAD_INIT(apk_path_hash_list);

struct my_dir_context {
    struct dir_context ctx;
    struct list_head *data_path_list;
    char *parent_dir;
    void *private_data;
    int depth;
    int *stop;
};
// https://docs.kernel.org/filesystems/porting.html
// filldir_t (readdir callbacks) calling conventions have changed. Instead of returning 0 or -E... it returns bool now. false means "no more" (as -E... used to) and true - "keep going" (as 0 in old calling conventions). Rationale: callers never looked at specific -E... values anyway. -> iterate_shared() instances require no changes at all, all filldir_t ones in the tree converted.
#if LINUX_VERSION_CODE >= KERNEL_VERSION(6, 1, 0)
#define FILLDIR_RETURN_TYPE bool
#define FILLDIR_ACTOR_CONTINUE true
#define FILLDIR_ACTOR_STOP false
#else
#define FILLDIR_RETURN_TYPE int
#define FILLDIR_ACTOR_CONTINUE 0
#define FILLDIR_ACTOR_STOP -EINVAL
#endif
FILLDIR_RETURN_TYPE my_actor(struct dir_context *ctx, const char *name, int namelen, loff_t off, u64 ino,
                             unsigned int d_type)
{
    struct my_dir_context *my_ctx = container_of(ctx, struct my_dir_context, ctx);
    char dirpath[DATA_PATH_LEN];

    if (!my_ctx) {
        pr_err("Invalid context\n");
        return FILLDIR_ACTOR_STOP;
    }
    if (my_ctx->stop && *my_ctx->stop) {
        pr_info("Stop searching\n");
        return FILLDIR_ACTOR_STOP;
    }

    if (!strncmp(name, "..", namelen) || !strncmp(name, ".", namelen))
        return FILLDIR_ACTOR_CONTINUE; // Skip "." and ".."

    if (d_type == DT_DIR && namelen >= 8 && !strncmp(name, "vmdl", 4) && !strncmp(name + namelen - 4, ".tmp", 4)) {
        pr_info("Skipping directory: %.*s\n", namelen, name);
        return FILLDIR_ACTOR_CONTINUE; // Skip staging package
    }

    if (snprintf(dirpath, DATA_PATH_LEN, "%s/%.*s", my_ctx->parent_dir, namelen, name) >= DATA_PATH_LEN) {
        pr_err("Path too long: %s/%.*s\n", my_ctx->parent_dir, namelen, name);
        return FILLDIR_ACTOR_CONTINUE;
    }

    if (d_type == DT_DIR && my_ctx->depth > 0 && (my_ctx->stop && !*my_ctx->stop)) {
        struct data_path *data = kzalloc(sizeof(struct data_path), GFP_KERNEL);

        if (!data) {
            pr_err("Failed to allocate memory for %s\n", dirpath);
            return FILLDIR_ACTOR_CONTINUE;
        }

        strscpy(data->dirpath, dirpath, DATA_PATH_LEN);
        data->depth = my_ctx->depth - 1;
        list_add_tail(&data->list, my_ctx->data_path_list);
    } else {
        if ((namelen == 8) && (strncmp(name, "base.apk", namelen) == 0)) {
            struct apk_path_hash *pos;
            unsigned int hash = full_name_hash(NULL, dirpath, strlen(dirpath));
            list_for_each_entry (pos, &apk_path_hash_list, list) {
                if (hash == pos->hash) {
                    pos->exists = true;
                    return FILLDIR_ACTOR_CONTINUE;
                }
            }

            u8 signature_index = 0;
            bool is_manager = is_manager_apk(dirpath, &signature_index);
            pr_info("Found new base.apk at path: %s, is_manager: %d\n", dirpath, is_manager);
            if (is_manager) {
                // Crown every matching package (static and dynamic signed
                // managers may coexist); do not stop the scan.
                crown_manager(dirpath, my_ctx->private_data, signature_index);
            } else {
                struct apk_path_hash *apk_data = kzalloc(sizeof(struct apk_path_hash), GFP_KERNEL);
                if (!apk_data) {
                    pr_err("Failed to allocate apk_path_hash for %s\n", dirpath);
                    return FILLDIR_ACTOR_CONTINUE;
                }
                apk_data->hash = hash;
                apk_data->exists = true;
                list_add_tail(&apk_data->list, &apk_path_hash_list);
            }
        }
    }

    return FILLDIR_ACTOR_CONTINUE;
}

void search_manager(const char *path, int depth, struct list_head *uid_data)
{
    int i, stop = 0;
    struct list_head data_path_list;
    INIT_LIST_HEAD(&data_path_list);
    unsigned long data_app_magic = 0;

    // Initialize APK cache list
    struct apk_path_hash *pos, *n;
    list_for_each_entry (pos, &apk_path_hash_list, list) {
        pos->exists = false;
    }

    // First depth
    struct data_path data;
    strscpy(data.dirpath, path, DATA_PATH_LEN);
    data.depth = depth;
    list_add_tail(&data.list, &data_path_list);

    for (i = depth; i >= 0; i--) {
        struct data_path *pos, *n;

        list_for_each_entry_safe (pos, n, &data_path_list, list) {
            struct my_dir_context ctx = { .ctx.actor = my_actor,
                                          .data_path_list = &data_path_list,
                                          .parent_dir = pos->dirpath,
                                          .private_data = uid_data,
                                          .depth = pos->depth,
                                          .stop = &stop };
            struct file *file;

            if (!stop) {
                file = filp_open(pos->dirpath, O_RDONLY | O_NOFOLLOW, 0);
                if (IS_ERR(file)) {
                    pr_err("Failed to open directory: %s, err: %ld\n", pos->dirpath, PTR_ERR(file));
                    goto skip_iterate;
                }

                // grab magic on first folder, which is /data/app
                if (!data_app_magic) {
                    if (file->f_inode->i_sb->s_magic) {
                        data_app_magic = file->f_inode->i_sb->s_magic;
                        pr_info("%s: dir: %s got magic! 0x%lx\n", __func__, pos->dirpath, data_app_magic);
                    } else {
                        filp_close(file, NULL);
                        goto skip_iterate;
                    }
                }

                if (file->f_inode->i_sb->s_magic != data_app_magic) {
                    pr_info("%s: skip: %s magic: 0x%lx expected: 0x%lx\n", __func__, pos->dirpath,
                            file->f_inode->i_sb->s_magic, data_app_magic);
                    filp_close(file, NULL);
                    goto skip_iterate;
                }

                iterate_dir(file, &ctx.ctx);
                filp_close(file, NULL);
            }
        skip_iterate:
            list_del(&pos->list);
            if (pos != &data)
                kfree(pos);
        }
    }

    // Remove stale cached APK entries
    list_for_each_entry_safe (pos, n, &apk_path_hash_list, list) {
        if (!pos->exists) {
            list_del(&pos->list);
            kfree(pos);
        }
    }
}

static bool is_uid_exist(uid_t uid, char *package, void *data)
{
    struct list_head *list = (struct list_head *)data;
    struct uid_data *np;

    bool exist = false;
    list_for_each_entry (np, list, list) {
        if (np->uid == uid % PER_USER_RANGE && strncmp(np->package, package, KSU_MAX_PACKAGE_NAME) == 0) {
            exist = true;
            break;
        }
    }
    return exist;
}

void track_throne(bool prune_only)
{
    struct file *fp = filp_open(SYSTEM_PACKAGES_LIST_PATH, O_RDONLY, 0);
    if (IS_ERR(fp)) {
        pr_err("%s: open " SYSTEM_PACKAGES_LIST_PATH " failed: %ld\n", __func__, PTR_ERR(fp));
        return;
    }

    struct list_head uid_list;
    INIT_LIST_HEAD(&uid_list);

    char chr = 0;
    loff_t pos = 0;
    loff_t line_start = 0;
    char buf[KSU_MAX_PACKAGE_NAME];
    for (;;) {
        ssize_t count = kernel_read(fp, &chr, sizeof(chr), &pos);
        if (count != sizeof(chr))
            break;
        if (chr != '\n')
            continue;

        count = kernel_read(fp, buf, sizeof(buf) - 1, &line_start);
        if (count <= 0) {
            break;
        }
        buf[count] = '\0';

        struct uid_data *data = kzalloc(sizeof(struct uid_data), GFP_KERNEL);
        if (!data) {
            filp_close(fp, 0);
            goto out;
        }

        char *tmp = buf;
        const char *delim = " ";
        char *package = strsep(&tmp, delim);
        char *uid = strsep(&tmp, delim);
        if (!uid || !package) {
            kfree(data);
            pr_err("update_uid: package or uid is NULL!\n");
            break;
        }

        u32 res;
        if (kstrtou32(uid, 10, &res)) {
            kfree(data);
            pr_err("update_uid: uid parse err\n");
            break;
        }
        data->uid = res;
        strscpy(data->package, package, sizeof(data->package));
        list_add_tail(&data->list, &uid_list);
        // reset line start
        line_start = pos;
    }
    filp_close(fp, 0);

    // now update uid list
    struct uid_data *np;
    struct uid_data *n;
    bool removed = false;

    if (prune_only)
        goto prune;

    // first, drop crowned managers whose package is gone; packages that are
    // still installed keep their throne.
    {
        unsigned long flags;
        int i;

        spin_lock_irqsave(&ksu_manager_list_lock, flags);
        for (i = 0; i < KSU_MAX_MANAGERS; i++) {
            bool exist = false;

            if (!ksu_manager_list[i].valid)
                continue;

            list_for_each_entry (np, &uid_list, list) {
                if (np->uid % KSU_PER_USER_RANGE == ksu_manager_list[i].appid) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                pr_info("manager(uid=%d) is uninstalled, remove it!\n", ksu_manager_list[i].appid);
                ksu_manager_list[i].valid = false;
                removed = true;
            }
        }
        spin_unlock_irqrestore(&ksu_manager_list_lock, flags);
    }

    if (removed) {
        // A manager was just uninstalled; as before, don't search again in
        // this round (its /data/app dir may still be around).
        goto prune;
    }

    // Search when no manager is crowned at all, or when the dynamic manager
    // sign is set but no dynamic-signed manager has been crowned yet.
    if (!ksu_is_manager_appid_valid() ||
        (ksu_is_dynamic_manager_enabled() && !ksu_has_manager_signature_index(KSU_SIGNATURE_INDEX_DYNAMIC_MANAGER))) {
        pr_info("Searching manager...\n");
        search_manager("/data/app", 2, &uid_list);
        pr_info("Search manager finished\n");
    }

prune:
    // then prune the allowlist
    ksu_prune_allowlist(is_uid_exist, &uid_list);
out:
    // free uid_list
    list_for_each_entry_safe (np, n, &uid_list, list) {
        list_del(&np->list);
        kfree(np);
    }
}

int ksu_handle_get_managers_cmd(struct ksu_get_managers_cmd __user *arg, struct ksu_get_managers_cmd *cmd)
{
    u16 max_allowed = cmd->count;
    int count = 0;
    int total = 0;
    int i;
    unsigned long flags;
    struct ksu_manager_entry snapshot[KSU_MAX_MANAGERS];

    // Snapshot under lock; copy_to_user may sleep.
    spin_lock_irqsave(&ksu_manager_list_lock, flags);
    for (i = 0; i < KSU_MAX_MANAGERS; i++) {
        if (!ksu_manager_list[i].valid)
            continue;
        snapshot[total].uid = ksu_manager_list[i].appid;
        snapshot[total].signature_index = ksu_manager_list[i].signature_index;
        total++;
    }
    spin_unlock_irqrestore(&ksu_manager_list_lock, flags);

    for (i = 0; i < total; i++) {
        if (count < max_allowed) {
            void __user *dest = (void __user *)((char *)arg + sizeof(struct ksu_get_managers_cmd) +
                                                (count * sizeof(struct ksu_manager_entry)));

            if (copy_to_user(dest, &snapshot[i], sizeof(snapshot[i]))) {
                return -EFAULT;
            }
            count++;
        }
    }

    cmd->total_count = total;
    return 0;
}

void __init ksu_throne_tracker_init()
{
    // nothing to do
}

void __exit ksu_throne_tracker_exit()
{
    struct apk_path_hash *pos, *n;

    list_for_each_entry_safe (pos, n, &apk_path_hash_list, list) {
        list_del(&pos->list);
        kfree(pos);
    }
}
