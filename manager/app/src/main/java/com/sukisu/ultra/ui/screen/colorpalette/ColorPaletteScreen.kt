package com.sukisu.ultra.ui.screen.colorpalette

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.theme.ColorMode
import com.sukisu.ultra.ui.theme.FontConfig
import com.sukisu.ultra.ui.theme.ThemeManager
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
    val fontPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        FontConfig.saveFontFile(context, uri)
    }
    val exportThemeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = ThemeManager.exportTheme(context, uri)
        android.widget.Toast.makeText(
            context,
            if (ok) R.string.settings_theme_export_success else R.string.settings_theme_export_failed,
            android.widget.Toast.LENGTH_SHORT,
        ).show()
    }

    // API 29+ 直接写 MediaStore Download，文件名完整保留 .mkt 后缀
    val exportThemeDirect: () -> Unit = directExport@{
        if (android.os.Build.VERSION.SDK_INT < 29) {
            runCatching { exportThemeLauncher.launch("MakoSU_theme.mkt") }
            return@directExport
        }
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "MakoSU_theme_${System.currentTimeMillis()}.mkt")
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = context.contentResolver.insert(
            android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values,
        )
        val ok = uri != null && ThemeManager.exportTheme(context, uri)
        if (!ok && uri != null) {
            runCatching { context.contentResolver.delete(uri, null, null) }
        }
        android.widget.Toast.makeText(
            context,
            if (ok) R.string.settings_theme_export_success else R.string.settings_theme_export_failed,
            android.widget.Toast.LENGTH_SHORT,
        ).show()
    }
    val importThemeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = ThemeManager.importTheme(context, uri)
        android.widget.Toast.makeText(
            context,
            if (ok) R.string.settings_theme_import_success else R.string.settings_theme_import_failed,
            android.widget.Toast.LENGTH_SHORT,
        ).show()
    }

    // Per-page background picker + change/clear dialog
    var pendingPageBgPage by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<Int?>(null)
    }
    var pageBgDialogPage by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf<Int?>(null)
    }
    val pageBgPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val page = pendingPageBgPage
        pendingPageBgPage = null
        if (uri != null && page != null) {
            com.sukisu.ultra.ui.theme.PageBackgrounds.saveImage(context, page, uri)
        }
    }

    pageBgDialogPage?.let { page ->
        val pageName = stringResource(
            when (page) {
                0 -> R.string.home
                1 -> R.string.superuser
                2 -> R.string.module
                else -> R.string.settings
            }
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pageBgDialogPage = null },
            title = { androidx.compose.material3.Text(pageName) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    pageBgDialogPage = null
                    pendingPageBgPage = page
                    runCatching { pageBgPicker.launch("image/*") }
                }) {
                    androidx.compose.material3.Text(stringResource(R.string.grid_card_image_change))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    com.sukisu.ultra.ui.theme.PageBackgrounds.clear(context, page)
                    pageBgDialogPage = null
                }) {
                    androidx.compose.material3.Text(stringResource(R.string.grid_card_image_clear))
                }
            },
        )
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
        customFontEnabled = FontConfig.isCustomFontEnabled,
        customFontFilename = FontConfig.customFontFilename,
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
        onSetCustomFontEnabled = { enabled ->
            FontConfig.setCustomFontEnabledState(enabled)
            FontConfig.save(context)
        },
        onSelectCustomFont = {
            runCatching { fontPicker.launch("*/*") }
        },
        onClearCustomFont = {
            FontConfig.clearFont(context)
        },
        onExportTheme = exportThemeDirect,
        onImportTheme = {
            runCatching { importThemeLauncher.launch(arrayOf("*/*")) }
        },
        onPageBackgroundClick = { page ->
            if (com.sukisu.ultra.ui.theme.PageBackgrounds.isSet(page)) {
                pageBgDialogPage = page
            } else {
                pendingPageBgPage = page
                runCatching { pageBgPicker.launch("image/*") }
            }
        },
    )

    ColorPaletteScreenMaterial(state, actions)
}
