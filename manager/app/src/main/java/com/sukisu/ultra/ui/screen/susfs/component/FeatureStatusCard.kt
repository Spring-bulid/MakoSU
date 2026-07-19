package com.sukisu.ultra.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sukisu.ultra.ui.screen.susfs.component.material.FeatureStatusCardMaterial
import com.sukisu.ultra.ui.screen.susfs.util.EnabledFeature

@Composable
fun FeatureStatusCard(
    feature: EnabledFeature,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    FeatureStatusCardMaterial(
            feature = feature,
            onRefresh = onRefresh,
            modifier = modifier
        )
}
