package com.sukisu.ultra.ui.screen.colorpalette

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.ExpressiveSwitch
import com.sukisu.ultra.ui.component.material.ThemeColorPicker
import com.sukisu.ultra.ui.component.material.ThemeModeSelector
import com.sukisu.ultra.ui.component.material.TonalCard
import com.sukisu.ultra.ui.component.material.TopBarBackButton
import com.sukisu.ultra.ui.component.material.expressiveTopAppBarColors
import com.sukisu.ultra.ui.navigation.NavMode
import com.sukisu.ultra.ui.screen.home.HomeLayout
import com.sukisu.ultra.ui.theme.ColorMode

/**
 * FolkPatch Appearance page structure — implemented with plain TonalCard/Column only.
 * Avoids SegmentedColumn custom Layout (was a crash source when opening this screen).
 */
@Composable
fun ColorPaletteScreenMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState = state.uiState
    val currentColorMode = state.currentColorMode
    val isDark = currentColorMode.isDark ||
        (currentColorMode.isSystem && isSystemInDarkTheme())
    val isAmoled = currentColorMode.isAmoled
    val wallpaperOn = uiState.customBackgroundEnabled

    ExpressiveScaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { TopBarBackButton(onClick = actions.onBack) },
                title = {
                    Text(
                        text = stringResource(R.string.settings_category_appearance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
    ) { paddingValues ->
        val bottomPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // 1) Night mode
            SectionTitle(stringResource(R.string.settings_appearance_night_mode))
            ThemeModeSelector(
                selectedMode = currentColorMode,
                onModeSelected = { mode ->
                    val target = if (
                        mode == ColorMode.DARK && currentColorMode == ColorMode.DARK_AMOLED
                    ) ColorMode.DARK_AMOLED else mode
                    actions.onSetColorMode(target)
                },
            )
            ThemeColorPicker(
                selectedKeyColor = uiState.keyColor,
                onColorSelected = actions.onSetKeyColor,
                isDarkTheme = isDark,
                isAmoled = isAmoled,
            )
            if (isDark || isAmoled) {
                SwitchCard(
                    icon = Icons.Filled.DarkMode,
                    title = stringResource(R.string.settings_amoled_theme),
                    summary = stringResource(R.string.settings_amoled_theme_desc),
                    checked = isAmoled,
                    enabled = !wallpaperOn,
                    onCheckedChange = { on ->
                        actions.onSetColorMode(if (on) ColorMode.DARK_AMOLED else ColorMode.DARK)
                    },
                )
            }

            // 2) Layout
            SectionTitle(stringResource(R.string.settings_appearance_layout))
            TonalCard {
                Column {
                    HomeLayout.entries.forEachIndexed { index, layout ->
                        val icon = when (layout) {
                            HomeLayout.Stats -> Icons.Filled.Dashboard
                            HomeLayout.Pure -> Icons.Filled.Home
                        }
                        RadioRow(
                            icon = icon,
                            title = stringResource(layout.titleRes),
                            summary = stringResource(layout.summaryRes),
                            selected = state.homeLayout == layout,
                            onClick = { actions.onSetHomeLayout(layout) },
                            showDivider = index < HomeLayout.entries.lastIndex,
                        )
                    }
                }
            }
            TonalCard {
                Column {
                    NavMode.entries.forEachIndexed { index, mode ->
                        RadioRow(
                            icon = null,
                            title = stringResource(mode.titleRes),
                            summary = stringResource(mode.summaryRes),
                            selected = state.navMode == mode,
                            onClick = { actions.onSetNavMode(mode) },
                            showDivider = index < NavMode.entries.lastIndex,
                        )
                    }
                }
            }
            if (state.navMode == NavMode.Floating) {
                SwitchCard(
                    title = stringResource(R.string.settings_floating_auto_hide),
                    summary = stringResource(R.string.settings_floating_auto_hide_summary),
                    checked = state.floatingAutoHide,
                    onCheckedChange = actions.onSetFloatingAutoHide,
                )
                SwitchCard(
                    title = stringResource(R.string.settings_floating_swipe_hide),
                    summary = stringResource(R.string.settings_floating_swipe_hide_summary),
                    checked = state.floatingSwipeHide,
                    onCheckedChange = actions.onSetFloatingSwipeHide,
                )
            }

            // 3) Background
            SectionTitle(stringResource(R.string.settings_appearance_background))
            SwitchCard(
                icon = Icons.Filled.Image,
                title = stringResource(R.string.settings_custom_background),
                summary = stringResource(R.string.settings_custom_background_summary),
                checked = uiState.customBackgroundEnabled,
                onCheckedChange = actions.onSetCustomBackgroundEnabled,
            )
            if (uiState.customBackgroundEnabled) {
                TonalCard(onClick = actions.onSelectCustomBackground) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Image, null, Modifier.size(24.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                stringResource(R.string.settings_custom_background_select),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                stringResource(R.string.settings_custom_background_select_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (!uiState.customBackgroundUri.isNullOrBlank()) {
                    TonalCard(onClick = actions.onClearCustomBackground) {
                        Text(
                            stringResource(R.string.settings_custom_background_clear),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                BackgroundSliders(
                    opacity = uiState.customBackgroundOpacity,
                    blur = uiState.customBackgroundBlur,
                    dim = uiState.customBackgroundDim,
                    onOpacity = actions.onSetCustomBackgroundOpacity,
                    onBlur = actions.onSetCustomBackgroundBlur,
                    onDim = actions.onSetCustomBackgroundDim,
                )
            }

            // 4) Module banners (FolkPatch freedom)
            SectionTitle(stringResource(R.string.settings_appearance_banner))
            SwitchCard(
                icon = Icons.Filled.Wallpaper,
                title = stringResource(R.string.settings_module_banner_enabled),
                summary = stringResource(R.string.settings_module_banner_enabled_summary),
                checked = state.moduleBannerEnabled,
                onCheckedChange = actions.onSetModuleBannerEnabled,
            )
            if (state.moduleBannerEnabled) {
                SwitchCard(
                    title = stringResource(R.string.settings_module_banner_custom),
                    summary = stringResource(R.string.settings_module_banner_custom_summary),
                    checked = state.moduleBannerCustomEnabled,
                    onCheckedChange = actions.onSetModuleBannerCustomEnabled,
                )
                SwitchCard(
                    title = stringResource(R.string.settings_module_banner_custom_opacity),
                    summary = stringResource(R.string.settings_module_banner_custom_opacity_summary),
                    checked = state.moduleBannerCustomOpacityEnabled,
                    onCheckedChange = actions.onSetModuleBannerCustomOpacityEnabled,
                )
                if (state.moduleBannerCustomOpacityEnabled) {
                    BannerOpacityCard(
                        opacity = state.moduleBannerOpacity,
                        onOpacity = actions.onSetModuleBannerOpacity,
                    )
                }
            }

            // 5) Display
            SectionTitle(stringResource(R.string.settings_appearance_display))
            SwitchCard(
                icon = Icons.Rounded.Fingerprint,
                title = stringResource(R.string.settings_show_fullstatus),
                summary = stringResource(R.string.settings_show_fullstatus_summary),
                checked = state.showFullStatus,
                onCheckedChange = actions.onSetShowFullStatus,
            )
            PageScaleCard(
                pageScale = uiState.pageScale,
                onSetPageScale = actions.onSetPageScale,
            )

            Spacer(Modifier.height(16.dp + bottomPad))
        }
    }
}

@Composable
private fun BannerOpacityCard(
    opacity: Float,
    onOpacity: (Float) -> Unit,
) {
    var local by remember(opacity) { mutableFloatStateOf(opacity) }
    TonalCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_module_banner_opacity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${(local * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = local,
                onValueChange = { local = it },
                onValueChangeFinished = { onOpacity(local) },
                valueRange = 0.05f..1f,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun SwitchCard(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    TonalCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.38f,
                    ),
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (enabled) 1f else 0.38f,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.38f,
                    ),
                )
            }
            ExpressiveSwitch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = null,
            )
        }
    }
}

@Composable
private fun RadioRow(
    icon: ImageVector?,
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    null,
                    Modifier.size(24.dp),
                    MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(selected = selected, onClick = null)
        }
        if (showDivider) {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun BackgroundSliders(
    opacity: Float,
    blur: Float,
    dim: Float,
    onOpacity: (Float) -> Unit,
    onBlur: (Float) -> Unit,
    onDim: (Float) -> Unit,
) {
    var o by remember(opacity) { mutableFloatStateOf(opacity) }
    var b by remember(blur) { mutableFloatStateOf(blur) }
    var d by remember(dim) { mutableFloatStateOf(dim) }
    TonalCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SliderLabeled(
                stringResource(R.string.settings_custom_background_opacity),
                "${(o * 100).toInt()}%",
                o, 0.2f..1f, { o = it }, { onOpacity(o) },
            )
            SliderLabeled(
                stringResource(R.string.settings_custom_background_blur),
                "${b.toInt()} dp",
                b, 0f..24f, { b = it }, { onBlur(b) },
            )
            SliderLabeled(
                stringResource(R.string.settings_custom_background_dim),
                "${(d * 100).toInt()}%",
                d, 0f..0.3f, { d = it }, { onDim(d) },
            )
        }
    }
}

@Composable
private fun SliderLabeled(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    onFinished: () -> Unit,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(valueText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            onValueChangeFinished = onFinished,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PageScaleCard(
    pageScale: Float,
    onSetPageScale: (Float) -> Unit,
) {
    var v by remember(pageScale) { mutableFloatStateOf(pageScale) }
    TonalCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AspectRatio, null, Modifier.size(24.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.settings_page_scale),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.settings_page_scale_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("${(v * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            }
            Slider(
                value = v,
                onValueChange = { v = it },
                onValueChangeFinished = { onSetPageScale(v) },
                valueRange = 0.8f..1.1f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
