package com.sukisu.ultra.ui.screen.susfs.content

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.content.material.KstatConfigContentMaterial

@Composable
fun KstatConfigContent(
    kstatConfigs: Set<String>,
    addKstatPaths: Set<String>,
    isLoading: Boolean,
    onAddKstatStatically: () -> Unit,
    onAddKstat: () -> Unit,
    onRemoveKstatConfig: (String) -> Unit,
    onEditKstatConfig: ((String) -> Unit)? = null,
    onRemoveAddKstat: (String) -> Unit,
    onEditAddKstat: ((String) -> Unit)? = null,
    onUpdateKstat: (String) -> Unit,
    onUpdateKstatFullClone: (String) -> Unit
) {
    KstatConfigContentMaterial(
            kstatConfigs = kstatConfigs,
            addKstatPaths = addKstatPaths,
            isLoading = isLoading,
            onAddKstatStatically = onAddKstatStatically,
            onAddKstat = onAddKstat,
            onRemoveKstatConfig = onRemoveKstatConfig,
            onEditKstatConfig = { path -> onEditKstatConfig?.invoke(path) },
            onRemoveAddKstat = onRemoveAddKstat,
            onEditAddKstat = { path -> onEditAddKstat?.invoke(path) },
            onUpdateKstat = onUpdateKstat,
            onUpdateKstatFullClone = onUpdateKstatFullClone
        )
}
