package com.sukisu.ultra.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.component.material.AddKstatStaticallyDialogMaterial

@Composable
fun AddKstatStaticallyDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    isLoading: Boolean,
    initialConfig: String = ""
) {
    AddKstatStaticallyDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            isLoading = isLoading,
            initialConfig = initialConfig
        )
}
