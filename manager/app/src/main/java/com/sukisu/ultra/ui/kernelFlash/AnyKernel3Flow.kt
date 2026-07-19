package com.sukisu.ultra.ui.kernelFlash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.sukisu.ultra.ui.screen.install.InstallMethod

@Composable
fun rememberAnyKernel3State(
    installMethodState: MutableState<InstallMethod?>,
    preselectedKernelUri: String?,
    horizonKernelSummary: String,
    isAbDevice: Boolean
) = rememberAnyKernel3StateMaterial(
    installMethodState = installMethodState,
    preselectedKernelUri = preselectedKernelUri,
    horizonKernelSummary = horizonKernelSummary,
    isAbDevice = isAbDevice,
)

@Composable
fun KpmPatchSelectionDialog(
    show: Boolean,
    currentOption: KpmPatchOption,
    onDismiss: () -> Unit,
    onOptionSelected: (KpmPatchOption) -> Unit,
) {
    KpmPatchSelectionDialogMaterial(
        show = show,
        currentOption = currentOption,
        onDismiss = onDismiss,
        onOptionSelected = onOptionSelected,
    )
}


