package com.sukisu.ultra.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.KernelVersion
import com.sukisu.ultra.Natives
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.dialog.rememberConfirmDialog
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.TonalCard
import com.sukisu.ultra.ui.component.material.expressiveTopAppBarColors
import com.sukisu.ultra.ui.component.rebootlistpopup.RebootListPopup
import com.sukisu.ultra.ui.component.statustag.StatusTag
import com.sukisu.ultra.ui.util.HomeBottomSpacer

@Composable
fun HomePagerMaterial(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    ExpressiveScaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            // FolkPatch home column: spacedBy(16.dp)
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeWarningSection(state = state, actions = actions)

            // key forces full dispose/recreate when layout style changes (avoids sticky composition)
            key(state.homeLayout) {
                when (state.homeLayout) {
                    HomeLayout.Stats -> FolkHomeScreenStats(state = state, actions = actions)
                    HomeLayout.Pure -> HomeScreenPure(state = state, actions = actions)
                }
            }
            // FolkPatch exact HomeBottomSpacer only (do NOT also add bottomInnerPadding —
            // floating pad is handled here; fixed bottom uses MainActivity 80.dp outer pad)
            HomeBottomSpacer()
        }
    }
}

@Composable
private fun HomeWarningSection(
    state: HomeUiState,
    actions: HomeActions,
) {
    if (state.checkUpdateEnabled) {
        UpdateCard(state = state, actions = actions)
    }
    if (state.showManagerPrBuildWarning && state.showFullStatus) {
        WarningCard(stringResource(id = R.string.home_pr_build_warning))
    } else if (state.showKernelPrBuildWarning && state.showFullStatus) {
        WarningCard(stringResource(id = R.string.home_pr_kernel_warning))
    }
    if (state.showVersionMismatchWarning && state.showFullStatus) {
        WarningCard(
            stringResource(
                id = R.string.home_version_mismatch,
                state.currentManagerVersionCode,
                state.ksuVersion ?: 0
            )
        )
    }
    if (state.showUAPIMisMatchWarning && state.showFullStatus) {
        WarningCard(
            stringResource(
                id = R.string.uapi_mismatch,
                state.managerUAPIVersion,
                state.kernelUAPIVersion ?: 0,
            )
        )
    }
    if (state.showRequireKernelWarning && state.showFullStatus) {
        if (state.currentManagerVersionCode < (state.ksuVersion ?: 0)) {
            WarningCard(
                stringResource(
                    id = R.string.require_manager_version,
                    state.currentManagerVersionCode,
                    state.ksuVersion ?: 0,
                )
            )
        } else {
            WarningCard(
                stringResource(
                    id = R.string.require_kernel_version,
                    state.ksuVersion ?: 0,
                    Natives.MINIMAL_SUPPORTED_KERNEL
                )
            )
        }
    }
    if (state.showRootWarning) {
        WarningCard(stringResource(id = R.string.grant_root_failed))
    }
}

@Composable
private fun UpdateCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val newVersion = state.latestVersionInfo
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)

    AnimatedVisibility(
        visible = state.hasUpdate,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { actions.onOpenUrl(newVersion.downloadUrl) })
        WarningCard(
            message = stringResource(id = R.string.new_version_available, newVersion.versionCode),
            MaterialTheme.colorScheme.outlineVariant
        ) {
            if (newVersion.changelog.isEmpty()) {
                actions.onOpenUrl(newVersion.downloadUrl)
            } else {
                updateDialog.showConfirm(
                    title = title,
                    content = newVersion.changelog,
                    markdown = true,
                    confirm = updateText
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeFlexibleTopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = { RebootListPopup() },
        colors = expressiveTopAppBarColors(),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun WarningCard(
    message: String,
    color: Color = MaterialTheme.colorScheme.errorContainer,
    onClick: (() -> Unit)? = null
) {
    val content = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
    if (onClick != null) {
        TonalCard(containerColor = color, onClick = onClick, content = content)
    } else {
        TonalCard(containerColor = color, content = content)
    }
}

@Preview(name = "PureUI", showBackground = true)
@Composable
private fun PureHomePreview() {
    HomeScreenPure(
        state = previewHomeScreenState(ksuVersion = 12345, lkmMode = true, superuserCount = 5, moduleCount = 10),
        actions = HomeActions({}, {}, {}, {}),
    )
}


private val previewSystemInfo = SystemInfo(
    kernelVersion = "6.1.0-android14-0-g1234567",
    managerVersion = "1.0.0 (10000)",
    deviceModel = "Google Pixel 6 Pro",
    kernelFullVersion = "v4.1.2-abc1234@main",
    fingerprint = "google/raven/raven:14/AP1A.240305.019:user/release-keys",
    selinuxStatus = "Enforcing",
    seccompStatus = 2,
)

private fun previewHomeScreenState(
    ksuVersion: Int?,
    lkmMode: Boolean?,
    isSafeMode: Boolean = false,
    isLateLoadMode: Boolean = false,
    superuserCount: Int = 0,
    moduleCount: Int = 0,
    selinuxStatus: String = "Enforcing",
) = HomeUiState(
    kernelVersion = KernelVersion(6, 1, 0),
    ksuVersion = ksuVersion,
    lkmMode = lkmMode,
    isManager = true,
    isManagerPrBuild = false,
    isKernelPrBuild = false,
    requiresNewKernel = false,
    isRootAvailable = ksuVersion != null,
    isSafeMode = isSafeMode,
    isLateLoadMode = isLateLoadMode,
    checkUpdateEnabled = false,
    showFullStatus = true,
    latestVersionInfo = com.sukisu.ultra.ui.util.module.LatestVersionInfo(),
    currentManagerVersionCode = 10000,
    superuserCount = superuserCount,
    moduleCount = moduleCount,
    systemInfo = previewSystemInfo.copy(selinuxStatus = selinuxStatus),
    kernelUAPIVersion = 1,
    managerUAPIVersion = 1,
    uapiMismatch = false,
)
