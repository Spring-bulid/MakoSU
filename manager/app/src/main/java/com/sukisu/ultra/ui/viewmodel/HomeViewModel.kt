package com.sukisu.ultra.ui.viewmodel

import android.content.Context
import android.os.Build
import android.system.Os
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukisu.ultra.BuildConfig
import com.sukisu.ultra.Natives
import com.sukisu.ultra.data.repository.SettingsRepository
import com.sukisu.ultra.data.repository.SettingsRepositoryImpl
import com.sukisu.ultra.getKernelVersion
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.screen.home.HomeLayout
import com.sukisu.ultra.ui.screen.home.HomeUiState
import com.sukisu.ultra.ui.screen.home.SystemInfo
import com.sukisu.ultra.ui.screen.home.getManagerVersion
import com.sukisu.ultra.ui.util.checkNewVersion
import com.sukisu.ultra.ui.util.getModuleCount
import com.sukisu.ultra.ui.util.getSELinuxStatusRaw
import com.sukisu.ultra.ui.util.getSuperuserCount
import com.sukisu.ultra.ui.util.module.LatestVersionInfo
import com.sukisu.ultra.ui.util.resolveDeviceName
import com.sukisu.ultra.ui.util.rootAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Home data ViewModel. Layout skin is owned by [com.sukisu.ultra.ui.screen.home.HomePager]
 * (prefs + UiRefresh), same idea as FolkPatch HomeScreen local homeLayout + refreshTheme.
 * This VM must not race layout via SharedPreferences listeners.
 */
class HomeViewModel(
    private val settingsRepo: SettingsRepository = SettingsRepositoryImpl(),
) : ViewModel() {

    private val preferences = ksuApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val previousLayout = _uiState.value.homeLayout
            val baseState = withContext(Dispatchers.IO) { buildState() }
            // Prefer in-memory layout already shown; only fall back to prefs.
            // Prevents async refresh finishing late and flashing a different skin.
            val layoutFromPrefs = HomeLayout.fromValue(settingsRepo.homeLayout)
            _uiState.update {
                baseState.copy(
                    homeLayout = layoutFromPrefs,
                    showFullStatus = preferences.getBoolean("show_fingerprint", true),
                    checkUpdateEnabled = settingsRepo.checkUpdate,
                )
            }
            // If prefs still match what UI was showing, keep identity (no-op for equals)
            if (layoutFromPrefs != previousLayout) {
                // prefs already applied above
            }
            if (settingsRepo.checkUpdate) {
                val latestVersionInfo = withContext(Dispatchers.IO) { checkNewVersion() }
                _uiState.update { it.copy(latestVersionInfo = latestVersionInfo) }
            }
        }
    }

    private fun buildState(): HomeUiState {
        val kernelVersion = getKernelVersion()
        val isManager = Natives.isManager
        val ksuVersion = if (isManager) Natives.version else null
        val kernelUAPIVersion = if (isManager) Natives.kernelUAPIVersion else null
        val managerUAPIVersion = Natives.managerUAPIVersion
        val lkmMode = ksuVersion?.let { if (kernelVersion.isGKI()) Natives.isLkmMode else null }
        val isRootAvailable = rootAvailable()
        val managerVersion = getManagerVersion(ksuApp)
        val kernelFullVersion = if (isManager) Natives.getFullVersion() else null

        return HomeUiState(
            kernelVersion = kernelVersion,
            ksuVersion = ksuVersion,
            lkmMode = lkmMode,
            isManager = isManager,
            isManagerPrBuild = BuildConfig.IS_PR_BUILD,
            isKernelPrBuild = Natives.isPrBuild,
            requiresNewKernel = isManager && Natives.requireNewKernel(),
            uapiMismatch = isManager && Natives.checkUAPIMismatch(),
            kernelUAPIVersion = kernelUAPIVersion,
            managerUAPIVersion = managerUAPIVersion,
            isRootAvailable = isRootAvailable,
            isSafeMode = Natives.isSafeMode,
            isLateLoadMode = Natives.isLateLoadMode,
            checkUpdateEnabled = settingsRepo.checkUpdate,
            showFullStatus = preferences.getBoolean("show_fingerprint", true),
            homeLayout = HomeLayout.fromValue(settingsRepo.homeLayout),
            latestVersionInfo = LatestVersionInfo(),
            currentManagerVersionCode = managerVersion.versionCode,
            superuserCount = getSuperuserCount(),
            moduleCount = getModuleCount(),
            systemInfo = SystemInfo(
                kernelVersion = Os.uname().release,
                managerVersion = "${managerVersion.versionName} (${managerVersion.versionCode}-${managerUAPIVersion})",
                deviceModel = resolveDeviceName(),
                kernelFullVersion = kernelFullVersion,
                fingerprint = Build.FINGERPRINT,
                selinuxStatus = getSELinuxStatusRaw(),
                seccompStatus = runCatching {
                    Os.prctl(21 /* PR_GET_SECCOMP */, 0, 0, 0, 0)
                }.getOrDefault(-1),
            ),
        )
    }
}
