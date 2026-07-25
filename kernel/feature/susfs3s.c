#include <linux/compiler.h>
#include <linux/cred.h>
#include <linux/dcache.h>
#include <linux/err.h>
#include <linux/fcntl.h>
#include <linux/file.h>
#include <linux/fs.h>
#include <linux/fs_struct.h>
#include <linux/path.h>
#include <linux/printk.h>
#include <linux/sched.h>
#include <linux/slab.h>
#include <linux/spinlock.h>
#include <linux/string.h>
#include <linux/uaccess.h>
#include <asm/stat.h>

#include "arch.h" // IWYU pragma: keep
#include "klog.h" // IWYU pragma: keep
#include "uapi/supercall.h"
#include "manager/manager_identity.h"
#include "feature/susfs3s.h"

// SUSFS3S: LKM-native subset of SuSFS-style measures (no kernel source patch).
// v1 implements two measures on top of the dispatcher-based syscall hooks:
//  - sus_path: registered absolute paths fail with -ENOENT in
//    openat/newfstatat/faccessat/statx for non-privileged callers.
//  - sus_kstat: successful newfstatat results for registered paths are
//    overlaid with a userspace-supplied stat template.

#define SUSFS3S_MAX_ENTRIES 64
#define SUSFS3S_PATH_LEN 256

struct sus_path_entry {
    bool used;
    char path[SUSFS3S_PATH_LEN];
};

struct sus_kstat_entry {
    bool used;
    char path[SUSFS3S_PATH_LEN];
    // Spoofed stat template. All fields are applied verbatim, so userspace
    // must send a complete template (e.g. a real stat with fields edited).
    __u64 st_ino;
    __u64 st_size;
    __u64 st_blksize;
    __u64 st_blocks;
    __s64 st_atime;
    __s64 st_mtime;
    __s64 st_ctime;
    __u32 st_mode;
    __u32 st_uid;
    __u32 st_gid;
    __u32 st_dev;
    __u32 st_nlink;
    __u32 st_rdev;
};

static struct sus_path_entry sus_paths[SUSFS3S_MAX_ENTRIES];
static struct sus_kstat_entry sus_kstats[SUSFS3S_MAX_ENTRIES];
// One lock for both lists; entries are fixed-size so no allocation happens
// under the lock and lookups stay O(64) strcmp worst case.
static DEFINE_SPINLOCK(susfs3s_lock);

// Registration counters for a lock-free empty-list fast path: the syscall
// hooks skip ALL work (including any path resolution) while a list is empty.
static int sus_path_count;
static int sus_kstat_count;

// In-place normalization: strip trailing slashes (keep "/" itself).
// Components like "." / ".." / duplicate slashes are intentionally NOT
// collapsed; registered paths are expected to be canonical absolute paths.
static void susfs3s_normalize_path(char *path)
{
    size_t len = strlen(path);

    while (len > 1 && path[len - 1] == '/') {
        path[--len] = '\0';
    }
}

// Copy the user path and resolve it to an absolute path for comparison.
// Relative paths are resolved against the dirfd (AT_FDCWD -> current cwd,
// otherwise the directory the fd points at) via d_path(). If resolution
// fails the path simply cannot match (fail-open: do not hide).
static int susfs3s_resolve_path(int dfd, const char __user *pathname, char *out, int outlen)
{
    char buf[SUSFS3S_PATH_LEN];
    long len;

    len = strncpy_from_user(buf, pathname, sizeof(buf));
    if (len <= 0)
        return -EFAULT;
    if (len >= (long)sizeof(buf))
        return -ENAMETOOLONG;
    buf[sizeof(buf) - 1] = '\0';

    if (buf[0] == '/') {
        strscpy(out, buf, outlen);
        susfs3s_normalize_path(out);
        return 0;
    }

    // Relative path: resolve dirfd to an absolute base, then append.
    {
        struct path base;
        char *tmp, *p;
        int ret = 0;

        if (dfd == AT_FDCWD) {
            struct fs_struct *fs = current->fs;

            // Kernel threads (kworkers, io_uring workers, ...) have no
            // fs_struct; dereferencing fs->lock would panic. Fail open.
            if (!fs)
                return -ESRCH;

            spin_lock(&fs->lock);
            base = fs->pwd;
            path_get(&base);
            spin_unlock(&fs->lock);
        } else {
            struct fd f = fdget(dfd);
            struct file *file;

#ifdef fd_file
            // 6.12+ made struct fd opaque; use the accessors.
            if (fd_empty(f))
                return -EBADF;
            file = fd_file(f);
#else
            if (!f.file)
                return -EBADF;
            file = f.file;
#endif
            base = file->f_path;
            path_get(&base);
            fdput(f);
        }

        tmp = kmalloc(1024, GFP_KERNEL);
        if (!tmp) {
            path_put(&base);
            return -ENOMEM;
        }
        p = d_path(&base, tmp, 1024);
        path_put(&base);
        if (IS_ERR(p)) {
            ret = PTR_ERR(p);
        } else if (!strcmp(p, "/")) {
            // Avoid producing "//rel" when the base is the root.
            ret = snprintf(out, outlen, "/%s", buf) >= outlen ? -ENAMETOOLONG : 0;
        } else {
            ret = snprintf(out, outlen, "%s/%s", p, buf) >= outlen ? -ENAMETOOLONG : 0;
        }
        kfree(tmp);
        if (ret)
            return ret;

        susfs3s_normalize_path(out);
        return 0;
    }
}

bool ksu_susfs3s_path_hidden(int dfd, const char __user *pathname)
{
    char resolved[SUSFS3S_PATH_LEN];
    unsigned long flags;
    bool hidden = false;
    int i;

    // Exemption: root and crowned managers always see the real filesystem.
    if (current_uid().val == 0 || is_manager())
        return false;

    // Fast path: nothing registered -> no resolution, no locking, no risk.
    if (!READ_ONCE(sus_path_count))
        return false;

    if (susfs3s_resolve_path(dfd, pathname, resolved, sizeof(resolved)))
        return false;

    spin_lock_irqsave(&susfs3s_lock, flags);
    for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
        if (sus_paths[i].used && !strcmp(sus_paths[i].path, resolved)) {
            hidden = true;
            break;
        }
    }
    spin_unlock_irqrestore(&susfs3s_lock, flags);

    if (hidden)
        pr_info("susfs3s: sus_path hit: %s (uid=%d)\n", resolved, current_uid().val);
    return hidden;
}

void ksu_susfs3s_try_spoof_kstat(int dfd, const char __user *pathname, void __user *statbuf)
{
    char resolved[SUSFS3S_PATH_LEN];
    struct sus_kstat_entry tmpl;
    unsigned long flags;
    bool found = false;
    int i;

    // No root/manager exemption here: like upstream SuSFS, the spoofed kstat
    // is what *everyone* (including the manager) is supposed to observe.
    // Fast path: nothing registered -> no resolution at all.
    if (!READ_ONCE(sus_kstat_count))
        return;

    if (susfs3s_resolve_path(dfd, pathname, resolved, sizeof(resolved)))
        return;

    spin_lock_irqsave(&susfs3s_lock, flags);
    for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
        if (sus_kstats[i].used && !strcmp(sus_kstats[i].path, resolved)) {
            memcpy(&tmpl, &sus_kstats[i], sizeof(tmpl));
            found = true;
            break;
        }
    }
    spin_unlock_irqrestore(&susfs3s_lock, flags);

    if (!found)
        return;

    // Read back the stat the real syscall just wrote, overlay the template,
    // and write it back. Template fields are applied verbatim; sub-second
    // timestamp precision is not part of the uapi, so nsec fields are zeroed.
    {
        struct stat st;

        if (copy_from_user(&st, statbuf, sizeof(st)))
            return;

        st.st_dev = tmpl.st_dev;
        st.st_ino = tmpl.st_ino;
        st.st_mode = tmpl.st_mode;
        st.st_nlink = tmpl.st_nlink;
        st.st_uid = tmpl.st_uid;
        st.st_gid = tmpl.st_gid;
        st.st_rdev = tmpl.st_rdev;
        st.st_size = tmpl.st_size;
        st.st_blksize = tmpl.st_blksize;
        st.st_blocks = tmpl.st_blocks;
        st.st_atime = tmpl.st_atime;
        st.st_mtime = tmpl.st_mtime;
        st.st_ctime = tmpl.st_ctime;
        st.st_atime_nsec = 0;
        st.st_mtime_nsec = 0;
        st.st_ctime_nsec = 0;

        if (copy_to_user(statbuf, &st, sizeof(st)))
            pr_warn("susfs3s: sus_kstat copy_to_user failed for %s\n", resolved);
        else
            pr_info("susfs3s: sus_kstat spoofed: %s (uid=%d)\n", resolved, current_uid().val);
    }
}

int ksu_handle_susfs3s_cmd(struct ksu_susfs3s_cmd *cmd)
{
    unsigned long flags;
    int i, ret = 0;

    cmd->path[sizeof(cmd->path) - 1] = '\0';

    switch (cmd->op) {
    case KSU_SUSFS3S_OP_ADD_SUS_PATH: {
        if (cmd->path[0] != '/')
            return -EINVAL;
        susfs3s_normalize_path(cmd->path);

        spin_lock_irqsave(&susfs3s_lock, flags);
        for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
            if (sus_paths[i].used && !strcmp(sus_paths[i].path, cmd->path)) {
                ret = -EEXIST;
                goto out_add_path;
            }
        }
        for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
            if (!sus_paths[i].used) {
                sus_paths[i].used = true;
                strscpy(sus_paths[i].path, cmd->path, sizeof(sus_paths[i].path));
                WRITE_ONCE(sus_path_count, sus_path_count + 1);
                pr_info("susfs3s: sus_path added: %s\n", cmd->path);
                goto out_add_path;
            }
        }
        ret = -ENOSPC;
    out_add_path:
        spin_unlock_irqrestore(&susfs3s_lock, flags);
        return ret;
    }

    case KSU_SUSFS3S_OP_DEL_SUS_PATH: {
        susfs3s_normalize_path(cmd->path);

        spin_lock_irqsave(&susfs3s_lock, flags);
        for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
            if (sus_paths[i].used && !strcmp(sus_paths[i].path, cmd->path)) {
                sus_paths[i].used = false;
                sus_paths[i].path[0] = '\0';
                WRITE_ONCE(sus_path_count, sus_path_count - 1);
                pr_info("susfs3s: sus_path removed: %s\n", cmd->path);
                goto out_del_path;
            }
        }
        ret = -ENOENT;
    out_del_path:
        spin_unlock_irqrestore(&susfs3s_lock, flags);
        return ret;
    }

    case KSU_SUSFS3S_OP_ADD_SUS_KSTAT: {
        if (cmd->path[0] != '/')
            return -EINVAL;
        susfs3s_normalize_path(cmd->path);

        spin_lock_irqsave(&susfs3s_lock, flags);
        // Existing path: update the template in place (ADD acts as set).
        for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
            if (sus_kstats[i].used && !strcmp(sus_kstats[i].path, cmd->path))
                goto fill_kstat;
        }
        for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
            if (!sus_kstats[i].used) {
                sus_kstats[i].used = true;
                strscpy(sus_kstats[i].path, cmd->path, sizeof(sus_kstats[i].path));
                WRITE_ONCE(sus_kstat_count, sus_kstat_count + 1);
                goto fill_kstat;
            }
        }
        ret = -ENOSPC;
        goto out_add_kstat;

    fill_kstat:
        sus_kstats[i].st_ino = cmd->st_ino;
        sus_kstats[i].st_size = cmd->st_size;
        sus_kstats[i].st_blksize = cmd->st_blksize;
        sus_kstats[i].st_blocks = cmd->st_blocks;
        sus_kstats[i].st_atime = cmd->st_atime;
        sus_kstats[i].st_mtime = cmd->st_mtime;
        sus_kstats[i].st_ctime = cmd->st_ctime;
        sus_kstats[i].st_mode = cmd->st_mode;
        sus_kstats[i].st_uid = cmd->st_uid;
        sus_kstats[i].st_gid = cmd->st_gid;
        sus_kstats[i].st_dev = cmd->st_dev;
        sus_kstats[i].st_nlink = cmd->st_nlink;
        sus_kstats[i].st_rdev = cmd->st_rdev;
        pr_info("susfs3s: sus_kstat set: %s\n", cmd->path);
    out_add_kstat:
        spin_unlock_irqrestore(&susfs3s_lock, flags);
        return ret;
    }

    case KSU_SUSFS3S_OP_DEL_SUS_KSTAT: {
        susfs3s_normalize_path(cmd->path);

        spin_lock_irqsave(&susfs3s_lock, flags);
        for (i = 0; i < SUSFS3S_MAX_ENTRIES; i++) {
            if (sus_kstats[i].used && !strcmp(sus_kstats[i].path, cmd->path)) {
                sus_kstats[i].used = false;
                sus_kstats[i].path[0] = '\0';
                WRITE_ONCE(sus_kstat_count, sus_kstat_count - 1);
                pr_info("susfs3s: sus_kstat removed: %s\n", cmd->path);
                goto out_del_kstat;
            }
        }
        ret = -ENOENT;
    out_del_kstat:
        spin_unlock_irqrestore(&susfs3s_lock, flags);
        return ret;
    }

    case KSU_SUSFS3S_OP_WIPE: {
        spin_lock_irqsave(&susfs3s_lock, flags);
        memset(sus_paths, 0, sizeof(sus_paths));
        memset(sus_kstats, 0, sizeof(sus_kstats));
        WRITE_ONCE(sus_path_count, 0);
        WRITE_ONCE(sus_kstat_count, 0);
        spin_unlock_irqrestore(&susfs3s_lock, flags);
        pr_info("susfs3s: wiped sus_path and sus_kstat lists\n");
        return 0;
    }

    default:
        pr_err("susfs3s: invalid op %u\n", cmd->op);
        return -EINVAL;
    }
}
