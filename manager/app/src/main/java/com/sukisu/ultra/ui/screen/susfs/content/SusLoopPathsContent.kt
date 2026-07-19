package com.sukisu.ultra.ui.screen.susfs.content

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.content.material.SusLoopPathsContentMaterial

@Composable
fun SusLoopPathsContent(
    susLoopPaths: Set<String>,
    isLoading: Boolean,
    onAddLoopPath: () -> Unit,
    onRemoveLoopPath: (String) -> Unit,
    onEditLoopPath: ((String) -> Unit)? = null,
    onReset: (() -> Unit)? = null
) {
    SusLoopPathsContentMaterial(
            susLoopPaths = susLoopPaths,
            isLoading = isLoading,
            onAddLoopPath = onAddLoopPath,
            onRemoveLoopPath = onRemoveLoopPath,
            onEditLoopPath = { path -> onEditLoopPath?.invoke(path) },
            onReset = { onReset?.invoke() }
        )
}
