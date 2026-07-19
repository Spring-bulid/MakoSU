package com.sukisu.ultra.ui.screen.susfs.content

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.content.material.SusPathsContentMaterial

@Composable
fun SusPathsContent(
    susPaths: Set<String>,
    isLoading: Boolean,
    onAddPath: () -> Unit,
    onAddAppPath: () -> Unit,
    onRemovePath: (String) -> Unit,
    onEditPath: ((String) -> Unit)? = null,
    forceRefreshApps: Boolean = false,
    onReset: (() -> Unit)? = null
) {
    SusPathsContentMaterial(
            susPaths = susPaths,
            isLoading = isLoading,
            onAddPath = onAddPath,
            onAddAppPath = onAddAppPath,
            onRemovePath = onRemovePath,
            onEditPath = { path -> onEditPath?.invoke(path) },
            onReset = { onReset?.invoke() }
        )
}
