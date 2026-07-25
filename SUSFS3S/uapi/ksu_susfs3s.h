/* SUSFS3S: LKM-native SuSFS subset (sus_path + sus_kstat) */
static const __u8 KSU_SUSFS3S_OP_ADD_SUS_PATH = 0;
static const __u8 KSU_SUSFS3S_OP_DEL_SUS_PATH = 1;
static const __u8 KSU_SUSFS3S_OP_ADD_SUS_KSTAT = 2;
static const __u8 KSU_SUSFS3S_OP_DEL_SUS_KSTAT = 3;
static const __u8 KSU_SUSFS3S_OP_WIPE = 4; /* wipe both lists */

struct ksu_susfs3s_cmd {
    __u8 op; /* Input: KSU_SUSFS3S_OP_* */
    __u8 pad[3];
    char path[256]; /* Input: absolute target path */
    /* kstat template for ADD_SUS_KSTAT; applied verbatim, must be complete */
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

/* IOCTL command definitions */
static const __u32 KSU_IOCTL_GRANT_ROOT = _IOC(_IOC_NONE, 'K', 1, 0);
static const __u32 KSU_IOCTL_GET_INFO = _IOR('K', 2, struct ksu_get_info_cmd);
/* deprecated */
static const __u32 KSU_IOCTL_GET_INFO_LEGACY = _IOC(_IOC_READ, 'K', 2, 0);
static const __u32 KSU_IOCTL_REPORT_EVENT = _IOC(_IOC_WRITE, 'K', 3, 0);
static const __u32 KSU_IOCTL_SET_SEPOLICY = _IOC(_IOC_READ | _IOC_WRITE, 'K', 4, 0);
static const __u32 KSU_IOCTL_CHECK_SAFEMODE = _IOC(_IOC_READ, 'K', 5, 0);
/* deprecated */
static const __u32 KSU_IOCTL_GET_ALLOW_LIST = _IOC(_IOC_READ | _IOC_WRITE, 'K', 6, 0);
/* deprecated */
static const __u32 KSU_IOCTL_GET_DENY_LIST = _IOC(_IOC_READ | _IOC_WRITE, 'K', 7, 0);
static const __u32 KSU_IOCTL_NEW_GET_ALLOW_LIST = _IOWR('K', 6, struct ksu_new_get_allow_list_cmd);
static const __u32 KSU_IOCTL_NEW_GET_DENY_LIST = _IOWR('K', 7, struct ksu_new_get_allow_list_cmd);
static const __u32 KSU_IOCTL_UID_GRANTED_ROOT = _IOC(_IOC_READ | _IOC_WRITE, 'K', 8, 0);
static const __u32 KSU_IOCTL_UID_SHOULD_UMOUNT = _IOC(_IOC_READ | _IOC_WRITE, 'K', 9, 0);
static const __u32 KSU_IOCTL_GET_MANAGER_APPID = _IOC(_IOC_READ, 'K', 10, 0);
static const __u32 KSU_IOCTL_GET_APP_PROFILE = _IOC(_IOC_READ | _IOC_WRITE, 'K', 11, 0);
static const __u32 KSU_IOCTL_SET_APP_PROFILE = _IOC(_IOC_WRITE, 'K', 12, 0);
static const __u32 KSU_IOCTL_GET_FEATURE = _IOC(_IOC_READ | _IOC_WRITE, 'K', 13, 0);
static const __u32 KSU_IOCTL_SET_FEATURE = _IOC(_IOC_WRITE, 'K', 14, 0);
static const __u32 KSU_IOCTL_GET_WRAPPER_FD = _IOC(_IOC_WRITE, 'K', 15, 0);
static const __u32 KSU_IOCTL_MANAGE_MARK = _IOC(_IOC_READ | _IOC_WRITE, 'K', 16, 0);
static const __u32 KSU_IOCTL_NUKE_EXT4_SYSFS = _IOC(_IOC_WRITE, 'K', 17, 0);
static const __u32 KSU_IOCTL_ADD_TRY_UMOUNT = _IOC(_IOC_WRITE, 'K', 18, 0);
static const __u32 KSU_IOCTL_SET_INIT_PGRP = _IO('K', 19);
static const __u32 KSU_IOCTL_GET_SULOG_FD = _IOW('K', 20, struct ksu_get_sulog_fd_cmd);
static const __u32 KSU_IOCTL_DISABLE_ESCAPE_TO_ROOT = _IO('K', 21);
// Other IOCTL command definitions
static const __u32 KSU_IOCTL_GET_FULL_VERSION = _IOC(_IOC_READ, 'K', 100, 0);
static const __u32 KSU_IOCTL_HOOK_TYPE = _IOC(_IOC_READ, 'K', 101, 0);
static const __u32 KSU_IOCTL_ENABLE_KPM = _IOC(_IOC_READ, 'K', 102, 0);
static const __u32 KSU_IOCTL_LIST_TRY_UMOUNT = _IOC(_IOC_READ | _IOC_WRITE, 'K', 103, 0);
static const __u32 KSU_IOCTL_SET_SPOOF_VERSION = _IOC(_IOC_WRITE, 'K', 104, 0);
static const __u32 KSU_IOCTL_DYNAMIC_MANAGER = _IOC(_IOC_READ | _IOC_WRITE, 'K', 105, 0);
static const __u32 KSU_IOCTL_GET_MANAGERS = _IOC(_IOC_READ | _IOC_WRITE, 'K', 106, 0);
static const __u32 KSU_IOCTL_SUSFS3S = _IOC(_IOC_READ | _IOC_WRITE, 'K', 107, 0);
static const __u32 KSU_IOCTL_KPM = _IOC(_IOC_READ | _IOC_WRITE, 'K', 200, 0);

#endif