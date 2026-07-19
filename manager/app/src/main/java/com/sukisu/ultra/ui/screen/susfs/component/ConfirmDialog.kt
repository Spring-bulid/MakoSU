package com.sukisu.ultra.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.component.material.ConfirmDialogMaterial

@Composable
fun ConfirmDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    titleRes: Int,
    messageRes: Int,
    isLoading: Boolean = false
) {
    ConfirmDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            titleRes = titleRes,
            messageRes = messageRes,
            isLoading = isLoading
        )
}
