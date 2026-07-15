#ifndef __KSU_H_UTIL
#define __KSU_H_UTIL

#include "linux/fdtable.h" // IWYU pragma: keep
#include <linux/version.h>
#include <linux/syscalls.h>
#include <linux/uaccess.h>

#if LINUX_VERSION_CODE >= KERNEL_VERSION(5, 11, 0)
#define ksu_close_fd close_fd
#else
#define ksu_close_fd ksys_close
#endif

#if LINUX_VERSION_CODE < KERNEL_VERSION(5, 8, 0)
#define ksu_strncpy_from_user_nofault strncpy_from_user
#define ksu_copy_from_user_nofault copy_from_user
#define ksu_copy_to_user_nofault copy_to_user
#else
#define ksu_strncpy_from_user_nofault strncpy_from_user_nofault
#define ksu_copy_from_user_nofault copy_from_user_nofault
#define ksu_copy_to_user_nofault copy_to_user_nofault
#endif

#endif
