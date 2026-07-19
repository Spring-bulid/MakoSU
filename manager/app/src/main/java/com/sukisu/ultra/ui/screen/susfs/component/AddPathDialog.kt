package com.sukisu.ultra.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.component.material.AddPathDialogMaterial

@Composable
fun AddPathDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean,
    titleRes: Int,
    labelRes: Int,
    initialValue: String = ""
) {
    AddPathDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            isLoading = isLoading,
            titleRes = titleRes,
            labelRes = labelRes,
            initialValue = initialValue
        )
}
