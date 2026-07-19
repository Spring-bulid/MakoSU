package com.sukisu.ultra.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.component.material.AddAppPathDialogMaterial
import com.sukisu.ultra.ui.screen.susfs.util.AppInfo

@Composable
fun AddAppPathDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    isLoading: Boolean,
    apps: List<AppInfo> = emptyList(),
    onLoadApps: () -> Unit,
    existingSusPaths: Set<String> = emptySet()
) {
    AddAppPathDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            isLoading = isLoading,
            apps = apps,
            onLoadApps = onLoadApps,
            existingSusPaths = existingSusPaths
        )
}
