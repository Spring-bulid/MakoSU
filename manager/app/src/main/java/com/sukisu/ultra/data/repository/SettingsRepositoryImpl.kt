package com.sukisu.ultra.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.edit
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.topjohnwu.superuser.ShellUtils
import com.sukisu.ultra.Natives
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.magica.BootCompletedReceiver
import com.sukisu.ultra.ui.screen.home.HomeLayout
import com.sukisu.ultra.ui.screen.modulerepo.RepoSort
import com.sukisu.ultra.ui.util.execKsud
import com.sukisu.ultra.ui.util.getFeaturePersistValue
import com.sukisu.ultra.ui.util.getFeatureStatus
import java.security.SecureRandom

class SettingsRepositoryImpl : SettingsRepository {

    private companion object {
        private const val INTENT_TOKEN_KEY = "intent_token"
        private val secureRandom = SecureRandom()
    }

    private val prefs by lazy {
        ksuApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    override var homeLayout: String
        get() = prefs.getString("home_layout", HomeLayout.DEFAULT_VALUE) ?: HomeLayout.DEFAULT_VALUE
        // commit=true: notify listeners immediately (apply can race with UI switches)
        set(value) = prefs.edit(commit = true) { putString("home_layout", value) }

    override var navMode: String
        get() = prefs.getString("nav_mode", "floating") ?: "floating"
        set(value) = prefs.edit(commit = true) { putString("nav_mode", value) }

    override var floatingAutoHide: Boolean
        get() = prefs.getBoolean("floating_auto_hide", true)
        set(value) = prefs.edit(commit = true) { putBoolean("floating_auto_hide", value) }

    override var floatingSwipeHide: Boolean
        get() = prefs.getBoolean("floating_swipe_hide", true)
        set(value) = prefs.edit(commit = true) { putBoolean("floating_swipe_hide", value) }

    override var checkUpdate: Boolean
        get() = prefs.getBoolean("check_update", true)
        set(value) = prefs.edit { putBoolean("check_update", value) }

    override var checkModuleUpdate: Boolean
        get() = prefs.getBoolean("module_check_update", true)
        set(value) = prefs.edit { putBoolean("module_check_update", value) }

    override var alternativeIcon : Boolean
        get() = prefs.getBoolean("use_alt_icon", false)
        set(value) = prefs.edit { putBoolean("use_alt_icon", value)}

    override var themeMode: Int
        get() = prefs.getInt("color_mode", 0)
        set(value) = prefs.edit { putInt("color_mode", value) }

    override var keyColor: Int
        get() = prefs.getInt("key_color", 0)
        set(value) = prefs.edit { putInt("key_color", value) }

    override var colorStyle: String
        get() = prefs.getString("color_style", PaletteStyle.TonalSpot.name) ?: PaletteStyle.TonalSpot.name
        set(value) = prefs.edit { putString("color_style", value) }

    override var colorSpec: String
        get() = prefs.getString("color_spec", ColorSpec.SpecVersion.SPEC_2025.name) ?: ColorSpec.SpecVersion.SPEC_2025.name
        set(value) = prefs.edit { putString("color_spec", value) }

    override var customBackgroundEnabled: Boolean
        get() = prefs.getBoolean("custom_background_enabled", false)
        set(value) = prefs.edit { putBoolean("custom_background_enabled", value) }

    override var customBackgroundUri: String?
        get() = prefs.getString("custom_background_uri", null)
        set(value) = prefs.edit { putString("custom_background_uri", value) }

    override var customBackgroundOpacity: Float
        get() = prefs.getFloat("custom_background_opacity", 1f).coerceIn(0f, 1f)
        set(value) = prefs.edit { putFloat("custom_background_opacity", value.coerceIn(0f, 1f)) }

    override var customBackgroundBlur: Float
        get() = prefs.getFloat("custom_background_blur", 6f).coerceIn(0f, 24f)
        set(value) = prefs.edit { putFloat("custom_background_blur", value.coerceIn(0f, 24f)) }

    override var customBackgroundDim: Float
        get() = prefs.getFloat("custom_background_dim", 0.05f).coerceIn(0f, 0.3f)
        set(value) = prefs.edit { putFloat("custom_background_dim", value.coerceIn(0f, 0.3f)) }

    override var moduleBannerEnabled: Boolean
        get() = prefs.getBoolean("module_banner_enabled", true)
        set(value) = prefs.edit(commit = true) { putBoolean("module_banner_enabled", value) }

    override var moduleBannerCustomEnabled: Boolean
        get() = prefs.getBoolean("module_banner_custom_enabled", true)
        set(value) = prefs.edit(commit = true) { putBoolean("module_banner_custom_enabled", value) }

    override var moduleBannerCustomOpacityEnabled: Boolean
        get() = prefs.getBoolean("module_banner_custom_opacity_enabled", false)
        set(value) = prefs.edit(commit = true) { putBoolean("module_banner_custom_opacity_enabled", value) }

    override var moduleBannerOpacity: Float
        // Default slightly higher than Folk 0.18 so banners are actually visible
        get() = prefs.getFloat("module_banner_opacity", 0.42f).coerceIn(0.05f, 1f)
        set(value) = prefs.edit(commit = true) { putFloat("module_banner_opacity", value.coerceIn(0.05f, 1f)) }

    override var pageScale: Float
        get() = prefs.getFloat("page_scale", 1.0f)
        set(value) = prefs.edit { putFloat("page_scale", value) }

    override var enableWebDebugging: Boolean
        get() = prefs.getBoolean("enable_web_debugging", false)
        set(value) = prefs.edit { putBoolean("enable_web_debugging", value) }

    override var moduleSortEnabledFirst: Boolean
        get() = prefs.getBoolean("module_sort_enabled_first", false)
        set(value) = prefs.edit { putBoolean("module_sort_enabled_first", value) }

    override var moduleSortActionFirst: Boolean
        get() = prefs.getBoolean("module_sort_action_first", false)
        set(value) = prefs.edit { putBoolean("module_sort_action_first", value) }

    override var moduleRepoSortOrder: Int
        get() = prefs.getInt("module_repo_sort_order", RepoSort.UPDATED.ordinal)
        set(value) = prefs.edit { putInt("module_repo_sort_order", value) }

    override var superuserShowSystemApps: Boolean
        get() = prefs.getBoolean("show_system_apps", false)
        set(value) = prefs.edit { putBoolean("show_system_apps", value) }

    override var superuserShowOnlyPrimaryUserApps: Boolean
        get() = prefs.getBoolean("show_only_primary_user_apps", false)
        set(value) = prefs.edit { putBoolean("show_only_primary_user_apps", value) }

    override var superuserSortOption: Int
        get() = prefs.getInt("superuser_sort_option", 0)
        set(value) = prefs.edit { putInt("superuser_sort_option", value) }

    override var suLogFilters: Set<String>?
        get() = prefs.getStringSet("sulog_filters", null)?.toSet()
        set(filters) = prefs.edit { putStringSet("sulog_filters", filters) }

    override var showFullStatus: Boolean
        get() = prefs.getBoolean("show_fingerprint", true)
        set(value) = prefs.edit { putBoolean("show_fingerprint", value) }

    override var autoJailbreak: Boolean
        get() = prefs.getBoolean("auto_jailbreak", false)
        set(value) {
            runCatching {
                ksuApp.packageManager.setComponentEnabledSetting(
                    ComponentName(ksuApp, BootCompletedReceiver::class.java),
                    if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }.onFailure {
                Log.e("Settings", "failed to change boot receiver state to $value", it)
            }
            prefs.edit {
                putBoolean("auto_jailbreak", value)
            }
        }

    override val intentToken: String
        get() {
        val existing = prefs.getString(INTENT_TOKEN_KEY, null)
        if (!existing.isNullOrBlank()) return existing
        val token = ByteArray(32).also(secureRandom::nextBytes)
            .joinToString(separator = "") { "%02x".format(it) }
        prefs.edit { putString(INTENT_TOKEN_KEY, token) }
        return token
    }

    override suspend fun getSuCompatStatus(): String = getFeatureStatus("su_compat")

    override suspend fun getSuCompatPersistValue(): Long? = getFeaturePersistValue("su_compat")

    override fun isSuEnabled(): Boolean = Natives.isSuEnabled()

    override fun setSuEnabled(enabled: Boolean): Boolean = Natives.setSuEnabled(enabled)

    override fun setSuCompatModePref(mode: Int) = prefs.edit { putInt("su_compat_mode", mode) }

    override fun getSuCompatModePref(): Int = prefs.getInt("su_compat_mode", 0)

    override suspend fun getKernelUmountStatus(): String = getFeatureStatus("kernel_umount")

    override fun isKernelUmountEnabled(): Boolean = Natives.isKernelUmountEnabled()

    override fun setKernelUmountEnabled(enabled: Boolean): Boolean = Natives.setKernelUmountEnabled(enabled)

    override suspend fun getSelinuxHideStatus(): String = getFeatureStatus("selinux_hide")

    override fun isSelinuxHideEnabled(): Boolean = Natives.isSelinuxHideEnabled()

    override fun setSelinuxHideEnabled(enabled: Boolean): Int = Natives.setSelinuxHideEnabled(enabled)

    override suspend fun getSulogStatus(): String = getFeatureStatus("sulog")

    override suspend fun getSulogPersistValue(): Long? = getFeaturePersistValue("sulog")

    override fun setSulogEnabled(enabled: Boolean): Boolean = execKsud("feature set sulog ${if (enabled) 1 else 0}", true)

    override suspend fun getAdbRootStatus(): String = getFeatureStatus("adb_root")

    override suspend fun getAdbRootPersistValue(): Long? = getFeaturePersistValue("adb_root")

    override fun setAdbRootEnabled(enabled: Boolean): Boolean =
        if (execKsud("feature set adb_root ${if (enabled) 1 else 0}", true)) {
            ShellUtils.fastCmd("setprop ctl.restart adbd")
            true
        } else {
            false
        }

    override fun isDefaultUmountModules(): Boolean = Natives.isDefaultUmountModules()

    override fun setDefaultUmountModules(enabled: Boolean): Boolean = Natives.setDefaultUmountModules(enabled)

    override fun isLkmMode(): Boolean = Natives.isLkmMode

    override fun execKsudFeatureSave() {
        execKsud("feature save", true)
    }
}
