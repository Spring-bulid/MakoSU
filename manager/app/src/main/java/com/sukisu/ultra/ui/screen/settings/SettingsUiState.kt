package com.sukisu.ultra.ui.screen.settings

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.sukisu.ultra.ui.navigation.NavMode
import com.sukisu.ultra.ui.screen.home.HomeLayout

@Immutable
data class SettingsUiState(
    val homeLayout: HomeLayout = HomeLayout.Pure,
    val navMode: NavMode = NavMode.Floating,
    val floatingAutoHide: Boolean = true,
    val floatingSwipeHide: Boolean = true,
    val checkUpdate: Boolean = true,
    val checkModuleUpdate: Boolean = true,
    val alternativeIcon: Boolean = false,
    val themeMode: Int = 0,
    val keyColor: Int = 0,
    val colorStyle: String = PaletteStyle.TonalSpot.name,
    val colorSpec: String = ColorSpec.SpecVersion.SPEC_2025.name,
    val customBackgroundEnabled: Boolean = false,
    val customBackgroundUri: String? = null,
    val customBackgroundOpacity: Float = 1f,
    val customBackgroundBlur: Float = 6f,
    val customBackgroundDim: Float = 0.05f,
    val moduleBannerEnabled: Boolean = true,
    val moduleBannerCustomEnabled: Boolean = true,
    val moduleBannerCustomOpacityEnabled: Boolean = false,
    val moduleBannerOpacity: Float = 0.42f,
    val pageScale: Float = 1.0f,
    val enableWebDebugging: Boolean = false,
    val showFullStatus: Boolean = true,

    val suCompatStatus: String = "",
    val suCompatMode: Int = 0,
    val isSuEnabled: Boolean = false,

    val kernelUmountStatus: String = "",
    val isKernelUmountEnabled: Boolean = false,

    val selinuxHideStatus: String = "",
    val isSelinuxHideEnabled: Boolean = false,

    val sulogStatus: String = "",
    val isSulogEnabled: Boolean = false,

    val isDefaultUmountModules: Boolean = false,

    val adbRootStatus: String = "",
    val isAdbRootEnabled: Boolean = false,

    val isLkmMode: Boolean = false,
    val isLateLoadMode: Boolean = false,

    val autoJailbreak: Boolean = false,
)

@Immutable
data class SettingsHomeActions(
    val onOpenGeneral: () -> Unit,
    val onOpenAppearance: () -> Unit,
    val onOpenFeatures: () -> Unit,
    val onOpenBehavior: () -> Unit,
    val onOpenModule: () -> Unit,
    val onOpenMore: () -> Unit,
)
