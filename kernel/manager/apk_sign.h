#ifndef __KSU_H_APK_V2_SIGN
#define __KSU_H_APK_V2_SIGN

#include <linux/types.h>

// Signature indexes for crowned managers (stored per manager in the
// crowned-manager list, see manager/manager_identity.h).
// 255 is reserved for the dynamic manager, 254 for ksu debug.
#define KSU_SIGNATURE_INDEX_STATIC 0
#define KSU_SIGNATURE_INDEX_STATIC2 1
#define KSU_SIGNATURE_INDEX_KSU_DEBUG 254
#define KSU_SIGNATURE_INDEX_DYNAMIC_MANAGER 255

bool is_manager_apk(char *path, u8 *signature_index);
int get_pkg_from_apk_path(char *pkg, const char *path);

#endif
