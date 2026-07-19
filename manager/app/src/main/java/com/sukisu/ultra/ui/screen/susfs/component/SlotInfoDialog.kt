package com.sukisu.ultra.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.sukisu.ultra.ui.screen.susfs.component.material.SlotInfoDialogMaterial
import com.sukisu.ultra.ui.screen.susfs.util.SlotInfo

@Composable
fun SlotInfoDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    slotInfoList: List<SlotInfo>,
    currentActiveSlot: String,
    isLoadingSlotInfo: Boolean,
    onRefresh: () -> Unit,
    onUseUname: (String) -> Unit,
    onUseBuildTime: (String) -> Unit
) {
    SlotInfoDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            slotInfoList = slotInfoList,
            currentActiveSlot = currentActiveSlot,
            isLoadingSlotInfo = isLoadingSlotInfo,
            onRefresh = onRefresh,
            onUseUname = onUseUname,
            onUseBuildTime = onUseBuildTime
        )
}
