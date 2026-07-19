package com.sukisu.ultra.ui.screen.susfs.content

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.content.material.EnabledFeaturesContentMaterial
import com.sukisu.ultra.ui.screen.susfs.util.EnabledFeature

@Composable
fun EnabledFeaturesContent(
    enabledFeatures: List<EnabledFeature>,
    onRefresh: () -> Unit
) {
    EnabledFeaturesContentMaterial(
            enabledFeatures = enabledFeatures,
            onRefresh = onRefresh
        )
}
