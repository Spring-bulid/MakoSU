package com.sukisu.ultra.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.sukisu.ultra.ui.navigation3.Navigator
import com.sukisu.ultra.ui.navigation3.Route

@Composable
fun SettingPager(
    navigator: Navigator,
    bottomInnerPadding: Dp
) {
    val actions = SettingsHomeActions(
        onOpenGeneral = { navigator.push(Route.SettingsGeneral) },
        onOpenAppearance = { navigator.push(Route.ColorPalette) },
        onOpenFeatures = { navigator.push(Route.SettingsFeatures) },
        onOpenBehavior = { navigator.push(Route.SettingsBehavior) },
        onOpenModule = { navigator.push(Route.SettingsModule) },
        onOpenMore = { navigator.push(Route.SettingsMore) },
    )

    SettingPagerMaterial(
        actions = actions,
        bottomInnerPadding = bottomInnerPadding,
    )
}
