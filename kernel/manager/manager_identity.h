#ifndef __KSU_H_MANAGER_IDENTITY
#define __KSU_H_MANAGER_IDENTITY

#include <linux/cred.h>
#include <linux/types.h>

#define KSU_INVALID_APPID -1
#define KSU_PER_USER_RANGE 100000

// Multi-manager model (aligned with ReSukiSU): up to KSU_MAX_MANAGERS crowned
// managers, each a {appid, signature_index} pair. Any crowned uid counts as a
// manager. signature_index: 0-252 static sign list, 253 toolkit, 254 debug,
// 255 dynamic manager.
#define KSU_MAX_MANAGERS 8

#ifdef CONFIG_KSU_DISABLE_MANAGER
static inline bool ksu_is_manager_appid_valid()
{
    return true;
}

static inline bool is_manager()
{
    return current_uid().val == 0;
}

static inline bool is_uid_manager(uid_t uid)
{
    return uid == 0;
}

static inline uid_t ksu_get_manager_appid()
{
    return 0;
}

static inline bool ksu_add_manager_appid(uid_t appid, u8 signature_index)
{
    (void)appid;
    (void)signature_index;
    return true;
}

static inline bool ksu_remove_manager_appid(uid_t appid)
{
    (void)appid;
    return true;
}

static inline void ksu_remove_manager_by_signature_index(u8 signature_index)
{
    (void)signature_index;
}

static inline bool ksu_has_manager_signature_index(u8 signature_index)
{
    (void)signature_index;
    return false;
}

static inline void ksu_invalidate_manager_uid()
{
}
#else
// Implemented in manager/throne_tracker.c on top of the crowned-manager list.
bool ksu_is_manager_appid_valid(void);
bool is_manager(void);
bool is_uid_manager(uid_t uid);
// First crowned appid, or KSU_INVALID_APPID when the list is empty. Kept for
// existing call sites that expect a single manager appid.
uid_t ksu_get_manager_appid(void);
bool ksu_add_manager_appid(uid_t appid, u8 signature_index);
bool ksu_remove_manager_appid(uid_t appid);
void ksu_remove_manager_by_signature_index(u8 signature_index);
bool ksu_has_manager_signature_index(u8 signature_index);
// Drop every crowned manager.
void ksu_invalidate_manager_uid(void);
#endif

#endif // __KSU_H_MANAGER_IDENTITY
