package com.sukisu.ultra.data.repository

interface SettingsRepository {
    var homeLayout: String
    /** FolkPatch nav_mode: floating | bottom | rail | auto */
    var navMode: String
    var floatingAutoHide: Boolean
    var floatingSwipeHide: Boolean
    var checkUpdate: Boolean
    var checkModuleUpdate: Boolean
    var alternativeIcon : Boolean
    var themeMode: Int
    var keyColor: Int
    var colorStyle: String
    var colorSpec: String
    var customBackgroundEnabled: Boolean
    var customBackgroundUri: String?
    var customBackgroundOpacity: Float
    var customBackgroundBlur: Float
    var customBackgroundDim: Float
    /** FolkPatch: show module card banners */
    var moduleBannerEnabled: Boolean
    /** FolkPatch: allow long-press custom folk banner */
    var moduleBannerCustomEnabled: Boolean
    /** FolkPatch: use custom banner image alpha */
    var moduleBannerCustomOpacityEnabled: Boolean
    /** FolkPatch bannerCustomOpacity 0..1 */
    var moduleBannerOpacity: Float
    var pageScale: Float
    var enableWebDebugging: Boolean
    var moduleSortEnabledFirst: Boolean
    var moduleSortActionFirst: Boolean
    var moduleRepoSortOrder: Int
    var superuserShowSystemApps: Boolean
    var superuserShowOnlyPrimaryUserApps: Boolean
    var superuserSortOption: Int
    var suLogFilters: Set<String>?
    var showFullStatus: Boolean
    var autoJailbreak: Boolean
    val intentToken: String

    suspend fun getSuCompatStatus(): String
    suspend fun getSuCompatPersistValue(): Long?
    fun isSuEnabled(): Boolean
    fun setSuEnabled(enabled: Boolean): Boolean
    fun setSuCompatModePref(mode: Int)
    fun getSuCompatModePref(): Int

    suspend fun getKernelUmountStatus(): String
    fun isKernelUmountEnabled(): Boolean
    fun setKernelUmountEnabled(enabled: Boolean): Boolean

    suspend fun getSelinuxHideStatus(): String
    fun isSelinuxHideEnabled(): Boolean
    fun setSelinuxHideEnabled(enabled: Boolean): Int

    suspend fun getSulogStatus(): String
    suspend fun getSulogPersistValue(): Long?
    fun setSulogEnabled(enabled: Boolean): Boolean

    suspend fun getAdbRootStatus(): String
    suspend fun getAdbRootPersistValue(): Long?
    fun setAdbRootEnabled(enabled: Boolean): Boolean

    fun isDefaultUmountModules(): Boolean
    fun setDefaultUmountModules(enabled: Boolean): Boolean

    fun isLkmMode(): Boolean

    fun execKsudFeatureSave()
}
