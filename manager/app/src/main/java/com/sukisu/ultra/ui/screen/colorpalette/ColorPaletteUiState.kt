package com.sukisu.ultra.ui.screen.colorpalette

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.sukisu.ultra.ui.navigation.NavMode
import com.sukisu.ultra.ui.screen.home.HomeLayout
import com.sukisu.ultra.ui.screen.settings.SettingsUiState
import com.sukisu.ultra.ui.theme.ColorMode

@Immutable
data class ColorPaletteUiState(
    val uiState: SettingsUiState,
    val currentColorMode: ColorMode,
    val currentPaletteStyle: PaletteStyle,
    val currentColorSpec: ColorSpec.SpecVersion,
    val showFullStatus: Boolean,
    val homeLayout: HomeLayout,
    val navMode: NavMode,
    val floatingAutoHide: Boolean,
    val floatingSwipeHide: Boolean,
    val moduleBannerEnabled: Boolean,
    val moduleBannerCustomEnabled: Boolean,
    val moduleBannerCustomOpacityEnabled: Boolean,
    val moduleBannerOpacity: Float,
)

@Immutable
data class ColorPaletteScreenActions(
    val onBack: () -> Unit,
    val onSetThemeMode: (Int) -> Unit,
    val onSetKeyColor: (Int) -> Unit,
    val onSetColorMode: (ColorMode) -> Unit,
    val onSetColorStyle: (String) -> Unit,
    val onSetColorSpec: (String) -> Unit,
    val onSetCustomBackgroundEnabled: (Boolean) -> Unit,
    val onSetCustomBackgroundOpacity: (Float) -> Unit,
    val onSetCustomBackgroundBlur: (Float) -> Unit,
    val onSetCustomBackgroundDim: (Float) -> Unit,
    val onSelectCustomBackground: () -> Unit,
    val onClearCustomBackground: () -> Unit,
    val onSetPageScale: (Float) -> Unit,
    val onSetShowFullStatus: (Boolean) -> Unit,
    val onSetHomeLayout: (HomeLayout) -> Unit,
    val onSetNavMode: (NavMode) -> Unit,
    val onSetFloatingAutoHide: (Boolean) -> Unit,
    val onSetFloatingSwipeHide: (Boolean) -> Unit,
    val onSetModuleBannerEnabled: (Boolean) -> Unit,
    val onSetModuleBannerCustomEnabled: (Boolean) -> Unit,
    val onSetModuleBannerCustomOpacityEnabled: (Boolean) -> Unit,
    val onSetModuleBannerOpacity: (Float) -> Unit,
)
