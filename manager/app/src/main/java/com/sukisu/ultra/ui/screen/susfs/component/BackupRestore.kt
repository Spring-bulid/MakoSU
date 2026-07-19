package com.sukisu.ultra.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.component.material.BackupRestoreComponentMaterial

@Composable
fun BackupRestoreComponent(
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onConfigReload: () -> Unit
) {
    BackupRestoreComponentMaterial(
            isLoading = isLoading,
            onLoadingChange = onLoadingChange,
            onConfigReload = onConfigReload
        )
}
