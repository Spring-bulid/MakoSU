package com.sukisu.ultra.ui.viewmodel

import android.content.Context
import android.system.OsConstants
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.sukisu.ultra.Natives
import com.sukisu.ultra.R
import com.sukisu.ultra.data.repository.SettingsRepository
import com.sukisu.ultra.data.repository.SettingsRepositoryImpl
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.screen.settings.SettingsUiState
import com.sukisu.ultra.ui.navigation.NavMode
import com.sukisu.ultra.ui.screen.home.HomeLayout
import com.sukisu.ultra.ui.theme.ColorMode

class SettingsViewModel(
    private val repo: SettingsRepository = SettingsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val checkUpdate = repo.checkUpdate
            val checkModuleUpdate = repo.checkModuleUpdate
            val alternativeIcon = repo.alternativeIcon
            val themeMode = repo.themeMode
            val keyColor = repo.keyColor
            val customBackgroundEnabled = repo.customBackgroundEnabled
            val customBackgroundUri = repo.customBackgroundUri
            val customBackgroundOpacity = repo.customBackgroundOpacity
            val customBackgroundBlur = repo.customBackgroundBlur
            val customBackgroundDim = repo.customBackgroundDim
            val moduleBannerEnabled = repo.moduleBannerEnabled
            val moduleBannerCustomEnabled = repo.moduleBannerCustomEnabled
            val moduleBannerCustomOpacityEnabled = repo.moduleBannerCustomOpacityEnabled
            val moduleBannerOpacity = repo.moduleBannerOpacity
            val pageScale = repo.pageScale
            val enableWebDebugging = repo.enableWebDebugging
            val showFullStatus = repo.showFullStatus
            val colorStyle = repo.colorStyle
            val colorSpec = repo.colorSpec
            val isLkmMode = repo.isLkmMode()

            // Async loading for natives/features
            val suCompatStatus = repo.getSuCompatStatus()
            val suCompatPersistValue = repo.getSuCompatPersistValue()
            val isSuEnabled = repo.isSuEnabled()

            val suCompatMode = if (suCompatPersistValue == 0L) 2 else if (!isSuEnabled) 1 else 0

            val kernelUmountStatus = repo.getKernelUmountStatus()
            val isKernelUmountEnabled = repo.isKernelUmountEnabled()
            val selinuxHideStatus = repo.getSelinuxHideStatus()
            val isSelinuxHideEnabled = repo.isSelinuxHideEnabled()
            val sulogStatus = repo.getSulogStatus()
            val isSulogEnabled = repo.getSulogPersistValue() == 1L
            val adbRootStatus = repo.getAdbRootStatus()
            val isAdbRootEnabled = repo.getAdbRootPersistValue() == 1L
            val isDefaultUmountModules = repo.isDefaultUmountModules()
            val homeLayout = HomeLayout.fromValue(repo.homeLayout)
            val navMode = NavMode.fromValue(repo.navMode)
            val floatingAutoHide = repo.floatingAutoHide
            val floatingSwipeHide = repo.floatingSwipeHide
            val autoJailbreak = repo.autoJailbreak
            val isLateLoadMode = Natives.isLateLoadMode

            _uiState.update {
                it.copy(
                    homeLayout = homeLayout,
                    navMode = navMode,
                    floatingAutoHide = floatingAutoHide,
                    floatingSwipeHide = floatingSwipeHide,
                    checkUpdate = checkUpdate,
                    checkModuleUpdate = checkModuleUpdate,
                    alternativeIcon = alternativeIcon,
                    themeMode = themeMode,
                    keyColor = keyColor,
                    customBackgroundEnabled = customBackgroundEnabled,
                    customBackgroundUri = customBackgroundUri,
                    customBackgroundOpacity = customBackgroundOpacity,
                    customBackgroundBlur = customBackgroundBlur,
                    customBackgroundDim = customBackgroundDim,
                    moduleBannerEnabled = moduleBannerEnabled,
                    moduleBannerCustomEnabled = moduleBannerCustomEnabled,
                    moduleBannerCustomOpacityEnabled = moduleBannerCustomOpacityEnabled,
                    moduleBannerOpacity = moduleBannerOpacity,
                    pageScale = pageScale,
                    enableWebDebugging = enableWebDebugging,
                    showFullStatus = showFullStatus,
                    colorStyle = colorStyle,
                    colorSpec = colorSpec,
                    suCompatStatus = suCompatStatus,
                    suCompatMode = suCompatMode,
                    isSuEnabled = isSuEnabled,
                    adbRootStatus = adbRootStatus,
                    isAdbRootEnabled = isAdbRootEnabled,
                    kernelUmountStatus = kernelUmountStatus,
                    isKernelUmountEnabled = isKernelUmountEnabled,
                    selinuxHideStatus = selinuxHideStatus,
                    isSelinuxHideEnabled = isSelinuxHideEnabled,
                    sulogStatus = sulogStatus,
                    isSulogEnabled = isSulogEnabled,
                    isDefaultUmountModules = isDefaultUmountModules,
                    isLkmMode = isLkmMode,
                    autoJailbreak = autoJailbreak,
                    isLateLoadMode = isLateLoadMode,
                )
            }
        }
    }

    fun setCheckUpdate(enabled: Boolean) {
        repo.checkUpdate = enabled
        _uiState.update { it.copy(checkUpdate = enabled) }
    }

    fun setHomeLayout(layout: HomeLayout) {
        // Write exactly the enum value; HomePager re-reads only via UiRefresh (no multi-listener race)
        repo.homeLayout = layout.value
        _uiState.update { it.copy(homeLayout = layout) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setNavMode(mode: NavMode) {
        repo.navMode = mode.value
        _uiState.update { it.copy(navMode = NavMode.fromValue(repo.navMode)) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setFloatingAutoHide(enabled: Boolean) {
        repo.floatingAutoHide = enabled
        _uiState.update { it.copy(floatingAutoHide = enabled) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setFloatingSwipeHide(enabled: Boolean) {
        repo.floatingSwipeHide = enabled
        _uiState.update { it.copy(floatingSwipeHide = enabled) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setCheckModuleUpdate(enabled: Boolean) {
        repo.checkModuleUpdate = enabled
        _uiState.update { it.copy(checkModuleUpdate = enabled) }
    }
    fun setAlternativeIcon(context: Context, enabled: Boolean) {
        repo.alternativeIcon = enabled
        _uiState.update { it.copy(alternativeIcon = enabled) }
        com.sukisu.ultra.ui.util.toggleLauncherIcon(context, enabled)
    }

    fun setThemeMode(mode: Int) {
        repo.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setColorMode(mode: ColorMode) {
        repo.themeMode = mode.value
        _uiState.update { it.copy(themeMode = mode.value) }
    }

    fun setKeyColor(color: Int) {
        repo.keyColor = color
        _uiState.update { it.copy(keyColor = color) }
    }

    fun setColorStyle(style: String) {
        repo.colorStyle = style
        _uiState.update { it.copy(colorStyle = style) }
    }

    fun setColorSpec(spec: String) {
        repo.colorSpec = spec
        _uiState.update { it.copy(colorSpec = spec) }
    }


    fun setCustomBackgroundEnabled(enabled: Boolean) {
        val effectiveEnabled = enabled && !repo.customBackgroundUri.isNullOrBlank()
        repo.customBackgroundEnabled = effectiveEnabled
        _uiState.update { it.copy(customBackgroundEnabled = effectiveEnabled) }
    }

    fun setCustomBackground(uri: String) {
        repo.customBackgroundUri = uri
        repo.customBackgroundEnabled = true
        _uiState.update {
            it.copy(
                customBackgroundEnabled = true,
                customBackgroundUri = uri,
            )
        }
    }

    fun clearCustomBackground() {
        repo.customBackgroundEnabled = false
        repo.customBackgroundUri = null
        _uiState.update {
            it.copy(
                customBackgroundEnabled = false,
                customBackgroundUri = null,
            )
        }
    }

    fun setCustomBackgroundOpacity(opacity: Float) {
        val value = opacity.coerceIn(0f, 1f)
        repo.customBackgroundOpacity = value
        _uiState.update { it.copy(customBackgroundOpacity = value) }
    }

    fun setCustomBackgroundBlur(blur: Float) {
        val value = blur.coerceIn(0f, 24f)
        repo.customBackgroundBlur = value
        _uiState.update { it.copy(customBackgroundBlur = value) }
    }

    fun setCustomBackgroundDim(dim: Float) {
        val value = dim.coerceIn(0f, 0.3f)
        repo.customBackgroundDim = value
        _uiState.update { it.copy(customBackgroundDim = value) }
    }

    fun setModuleBannerEnabled(enabled: Boolean) {
        repo.moduleBannerEnabled = enabled
        _uiState.update { it.copy(moduleBannerEnabled = enabled) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setModuleBannerCustomEnabled(enabled: Boolean) {
        repo.moduleBannerCustomEnabled = enabled
        _uiState.update { it.copy(moduleBannerCustomEnabled = enabled) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setModuleBannerCustomOpacityEnabled(enabled: Boolean) {
        repo.moduleBannerCustomOpacityEnabled = enabled
        _uiState.update { it.copy(moduleBannerCustomOpacityEnabled = enabled) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setModuleBannerOpacity(opacity: Float) {
        val value = opacity.coerceIn(0.05f, 1f)
        repo.moduleBannerOpacity = value
        _uiState.update { it.copy(moduleBannerOpacity = value) }
        com.sukisu.ultra.ui.theme.UiRefresh.bump()
    }

    fun setPageScale(scale: Float) {
        repo.pageScale = scale
        _uiState.update { it.copy(pageScale = scale) }
    }

    fun setEnableWebDebugging(enabled: Boolean) {
        repo.enableWebDebugging = enabled
        _uiState.update { it.copy(enableWebDebugging = enabled) }
    }

    fun setShowFullStatus(enabled: Boolean) {
        repo.showFullStatus = enabled
        _uiState.update { it.copy(showFullStatus = enabled) }
    }

    fun setSuCompatMode(mode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (mode) {
                0 -> if (repo.setSuEnabled(true)) {
                    repo.execKsudFeatureSave()
                    repo.setSuCompatModePref(0)
                    _uiState.update { it.copy(suCompatMode = 0, isSuEnabled = true) }
                }

                1 -> if (repo.setSuEnabled(true)) {
                    repo.execKsudFeatureSave()
                    if (repo.setSuEnabled(false)) {
                        // "Disable until reboot" implies it should be enabled on next boot.
                        // We set the preference to 0 (Enabled) to match the persistent state.
                        repo.setSuCompatModePref(0)
                        _uiState.update { it.copy(suCompatMode = 1, isSuEnabled = false) }
                    }
                }

                2 -> if (repo.setSuEnabled(false)) {
                    repo.execKsudFeatureSave()
                    repo.setSuCompatModePref(2)
                    _uiState.update { it.copy(suCompatMode = 2, isSuEnabled = false) }
                }
            }
        }
    }

    fun setKernelUmountEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setKernelUmountEnabled(enabled)) {
                repo.execKsudFeatureSave()
                _uiState.update { it.copy(isKernelUmountEnabled = enabled) }
            }
        }
    }

    fun setSelinuxHideEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val status = repo.setSelinuxHideEnabled(enabled)
            repo.execKsudFeatureSave()
            _uiState.update { it.copy(isSelinuxHideEnabled = enabled) }
            when (status) {
                0 -> {}
                -OsConstants.EAGAIN -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ksuApp, R.string.settings_selinux_hide_reboot_required,
                            Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ksuApp, ksuApp.getString(R.string.settings_selinux_hide_failed, status),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun setAutoJailbreak(enabled: Boolean) {
        repo.autoJailbreak = enabled
        _uiState.update { it.copy(autoJailbreak = enabled) }
    }

    fun setSulogEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setSulogEnabled(enabled)) {
                repo.execKsudFeatureSave()
                _uiState.update { it.copy(isSulogEnabled = enabled) }
            }
        }
    }

    fun setAdbRootEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setAdbRootEnabled(enabled)) {
                repo.execKsudFeatureSave()
                _uiState.update { it.copy(isAdbRootEnabled = enabled) }
            }
        }
    }

    fun setDefaultUmountModules(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repo.setDefaultUmountModules(enabled)) {
                _uiState.update { it.copy(isDefaultUmountModules = enabled) }
            }
        }
    }
}
