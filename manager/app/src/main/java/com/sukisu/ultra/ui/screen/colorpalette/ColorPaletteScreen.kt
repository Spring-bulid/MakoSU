package com.sukisu.ultra.ui.screen.colorpalette

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.theme.ColorMode
import com.sukisu.ultra.ui.viewmodel.SettingsViewModel

@Composable
fun ColorPaletteScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }

        uiState.customBackgroundUri
            ?.takeIf { it != uri.toString() }
            ?.let(Uri::parse)
            ?.let { oldUri ->
                runCatching {
                    context.contentResolver.releasePersistableUriPermission(
                        oldUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
            }

        viewModel.setCustomBackground(uri.toString())
    }
    val currentPaletteStyle = try {
        PaletteStyle.valueOf(uiState.colorStyle)
    } catch (_: Exception) {
        PaletteStyle.TonalSpot
    }
    val currentColorSpec = try {
        ColorSpec.SpecVersion.valueOf(uiState.colorSpec)
    } catch (_: Exception) {
        ColorSpec.SpecVersion.SPEC_2025
    }
    val state = ColorPaletteUiState(
        uiState = uiState,
        currentColorMode = ColorMode.fromValue(uiState.themeMode),
        currentPaletteStyle = currentPaletteStyle,
        currentColorSpec = currentColorSpec,
        showFullStatus = uiState.showFullStatus,
        homeLayout = uiState.homeLayout,
        navMode = uiState.navMode,
        floatingAutoHide = uiState.floatingAutoHide,
        floatingSwipeHide = uiState.floatingSwipeHide,
        moduleBannerEnabled = uiState.moduleBannerEnabled,
        moduleBannerCustomEnabled = uiState.moduleBannerCustomEnabled,
        moduleBannerCustomOpacityEnabled = uiState.moduleBannerCustomOpacityEnabled,
        moduleBannerOpacity = uiState.moduleBannerOpacity,
    )
    val actions = ColorPaletteScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSetThemeMode = viewModel::setThemeMode,
        onSetKeyColor = viewModel::setKeyColor,
        onSetColorMode = viewModel::setColorMode,
        onSetColorStyle = viewModel::setColorStyle,
        onSetColorSpec = viewModel::setColorSpec,
        onSetCustomBackgroundEnabled = { enabled ->
            if (enabled && uiState.customBackgroundUri.isNullOrBlank()) {
                backgroundPicker.launch(arrayOf("image/*"))
            } else {
                viewModel.setCustomBackgroundEnabled(enabled)
            }
        },
        onSetCustomBackgroundOpacity = viewModel::setCustomBackgroundOpacity,
        onSetCustomBackgroundBlur = viewModel::setCustomBackgroundBlur,
        onSetCustomBackgroundDim = viewModel::setCustomBackgroundDim,
        onSelectCustomBackground = {
            backgroundPicker.launch(arrayOf("image/*"))
        },
        onClearCustomBackground = {
            uiState.customBackgroundUri
                ?.let(Uri::parse)
                ?.let { uri ->
                    runCatching {
                        context.contentResolver.releasePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                }
            viewModel.clearCustomBackground()
        },
        onSetPageScale = viewModel::setPageScale,
        onSetShowFullStatus = viewModel::setShowFullStatus,
        onSetHomeLayout = viewModel::setHomeLayout,
        onSetNavMode = viewModel::setNavMode,
        onSetFloatingAutoHide = viewModel::setFloatingAutoHide,
        onSetFloatingSwipeHide = viewModel::setFloatingSwipeHide,
        onSetModuleBannerEnabled = viewModel::setModuleBannerEnabled,
        onSetModuleBannerCustomEnabled = viewModel::setModuleBannerCustomEnabled,
        onSetModuleBannerCustomOpacityEnabled = viewModel::setModuleBannerCustomOpacityEnabled,
        onSetModuleBannerOpacity = viewModel::setModuleBannerOpacity,
    )

    ColorPaletteScreenMaterial(state, actions)
}
