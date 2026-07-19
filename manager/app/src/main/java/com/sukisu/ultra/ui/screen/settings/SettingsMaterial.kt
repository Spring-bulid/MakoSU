package com.sukisu.ultra.ui.screen.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.SegmentedColumn
import com.sukisu.ultra.ui.component.material.SplicedSettingsItem
import com.sukisu.ultra.ui.component.material.expressiveTopAppBarColors

/**
 * FolkPatch-style settings home: compact title bar + single spliced category list.
 * Actual toggles live in category sub-pages.
 */
@Composable
fun SettingPagerMaterial(
    actions: SettingsHomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    ExpressiveScaffold(
        topBar = { SettingsHomeTopBar(scrollBehavior) },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    item(key = "general") {
                        SplicedSettingsItem(
                            icon = Icons.Filled.Settings,
                            title = stringResource(R.string.settings_category_general),
                            summary = stringResource(R.string.settings_category_general_summary),
                            onClick = actions.onOpenGeneral,
                        )
                    }
                    item(key = "appearance") {
                        SplicedSettingsItem(
                            icon = Icons.Filled.Palette,
                            title = stringResource(R.string.settings_category_appearance),
                            summary = stringResource(R.string.settings_category_appearance_summary),
                            onClick = actions.onOpenAppearance,
                        )
                    }
                    item(key = "features") {
                        SplicedSettingsItem(
                            icon = Icons.Filled.Security,
                            title = stringResource(R.string.settings_category_features),
                            summary = stringResource(R.string.settings_category_features_summary),
                            onClick = actions.onOpenFeatures,
                        )
                    }
                    item(key = "behavior") {
                        SplicedSettingsItem(
                            icon = Icons.Filled.Visibility,
                            title = stringResource(R.string.settings_category_behavior),
                            summary = stringResource(R.string.settings_category_behavior_summary),
                            onClick = actions.onOpenBehavior,
                        )
                    }
                    item(key = "module") {
                        SplicedSettingsItem(
                            icon = Icons.Filled.Extension,
                            title = stringResource(R.string.settings_category_module),
                            summary = stringResource(R.string.settings_category_module_summary),
                            onClick = actions.onOpenModule,
                        )
                    }
                    item(key = "more") {
                        SplicedSettingsItem(
                            icon = Icons.Filled.MoreHoriz,
                            title = stringResource(R.string.settings_category_more),
                            summary = stringResource(R.string.settings_category_more_summary),
                            onClick = actions.onOpenMore,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp + bottomInnerPadding))
            }
        }
    }
}

@Composable
private fun SettingsHomeTopBar(scrollBehavior: TopAppBarScrollBehavior?) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings),
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
}
