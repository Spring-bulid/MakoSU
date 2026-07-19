package com.sukisu.ultra.ui.kernelFlash.component

import androidx.compose.runtime.Composable

@Composable
fun SlotSelectionDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSlotSelected: (String) -> Unit,
) {
    SlotSelectionDialogMaterial(
            show = show,
            onDismiss = onDismiss,
            onSlotSelected = onSlotSelected
        )
}
