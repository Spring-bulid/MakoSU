package com.sukisu.ultra.ui.screen.kpm

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.material.ExpressiveDialog
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.SearchAppBar
import com.sukisu.ultra.ui.component.material.SnackBarHost
import com.sukisu.ultra.ui.component.material.TonalCard
import com.sukisu.ultra.ui.component.statustag.StatusTagMaterial
import com.sukisu.ultra.ui.viewmodel.KpmViewModel

/**
 * FolkPatch-style KPM page: ExpressiveScaffold + SearchAppBar, PullToRefreshBox,
 * TonalCard module items with status tags and tonal action buttons.
 * Business logic stays in KpmScreen / KpmViewModel; this file is UI only.
 */
@Composable
fun KpmMaterial(
    viewModel: KpmViewModel,
    actions: KpmActions,
    bottomInnerPadding: Dp = 0.dp
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackBarHost = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val showEmptyState by remember {
        derivedStateOf {
            state.moduleList.isEmpty() && !state.isRefreshing
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val pullToRefreshState = rememberPullToRefreshState()

    val kpmInstallMode = stringResource(R.string.kpm_install_mode)
    val kpmInstallModeLoad = stringResource(R.string.kpm_install_mode_load)
    val kpmInstallModeEmbed = stringResource(R.string.kpm_install_mode_embed)
    val cancel = stringResource(R.string.cancel)

    // FolkPatch/ModuleMaterial-style FAB expand-collapse on scroll
    val threshold = with(LocalDensity.current) { 100.dp.toPx() }
    val fabExpanded by remember {
        var lastIndex = 0
        var lastOffset = 0
        var scrollDelta = 0f
        var expanded = true
        derivedStateOf {
            val currentIndex = listState.firstVisibleItemIndex
            val currentOffset = listState.firstVisibleItemScrollOffset
            val delta = if (currentIndex == lastIndex) {
                (currentOffset - lastOffset).toFloat()
            } else if (currentIndex > lastIndex) {
                100f
            } else {
                -100f
            }
            scrollDelta = (scrollDelta + delta).coerceIn(-threshold, threshold)
            lastIndex = currentIndex
            lastOffset = currentOffset
            if (currentIndex == 0) {
                expanded = true
                scrollDelta = 0f
            } else if (expanded && scrollDelta >= threshold) {
                expanded = false
                scrollDelta = 0f
            } else if (!expanded && scrollDelta <= -threshold) {
                expanded = true
                scrollDelta = 0f
            }
            expanded
        }
    }

    if (state.showInstallModeDialog) {
        ExpressiveDialog(
            onDismissRequest = { actions.onDismissInstallDialog() },
            title = { Text(kpmInstallMode) },
            text = {
                Column {
                    state.tempModuleName?.let {
                        Text(text = stringResource(R.string.kpm_install_mode_description, it))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { actions.onConfirmInstall("", false) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.package_import),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(kpmInstallModeLoad)
                        }

                        Button(
                            onClick = { actions.onConfirmInstall("", true) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Code,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(kpmInstallModeEmbed)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { actions.onDismissInstallDialog() }) {
                    Text(cancel)
                }
            }
        )
    }

    ExpressiveScaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.kpm_title)) },
                searchText = state.searchStatus.searchText,
                onSearchTextChange = actions.onSearchTextChange,
                onClearClick = { actions.onSearchTextChange("") },
                actions = {
                    IconButton(
                        onClick = actions.onRefresh
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.refresh),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            SmallExtendedFloatingActionButton(
                modifier = Modifier.padding(bottom = bottomInnerPadding),
                expanded = fabExpanded,
                onClick = actions.onRequestInstall,
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.package_import),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = { Text(text = kpmInstallMode) },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
        snackbarHost = {
            SnackBarHost(
                hostState = snackBarHost,
                modifier = Modifier.padding(bottom = bottomInnerPadding)
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            isRefreshing = state.isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                actions.onRefresh()
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = state.isRefreshing,
                    state = pullToRefreshState,
                )
            },
        ) {
            if (showEmptyState) {
                EmptyStateViewMaterial()
            } else {
                KpmModuleList(
                    modules = if (state.searchStatus.searchText.isBlank()) state.moduleList else state.searchResults,
                    state = state,
                    actions = actions,
                    listState = listState,
                    showNotice = true,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp + bottomInnerPadding + 56.dp + 16.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun KpmModuleList(
    modules: List<KpmViewModel.ModuleInfo>,
    state: KpmUiState,
    actions: KpmActions,
    listState: LazyListState,
    showNotice: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    var isNoticeClosed by remember { mutableStateOf(sharedPreferences.getBoolean("is_notice_closed", false)) }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(13.dp),
        contentPadding = contentPadding,
    ) {
        if (showNotice && !isNoticeClosed) {
            item(key = "kpm_notice", contentType = "notice") {
                TonalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = stringResource(R.string.kernel_module_notice),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                                isNoticeClosed = true
                                sharedPreferences.edit { putBoolean("is_notice_closed", true) }
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close_notice),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        items(modules, key = { it.id }, contentType = { "kpm_module" }) { module ->
            KpmModuleItemMaterial(
                module = module,
                state = state,
                actions = actions,
                onUninstall = { actions.onRequestUninstall(module.id) }
            )
        }
    }
}

@Composable
private fun KpmModuleItemMaterial(
    module: KpmViewModel.ModuleInfo,
    state: KpmUiState,
    actions: KpmActions,
    onUninstall: () -> Unit
) {
    val showInputDialog = state.inputDialogState.visible && state.inputDialogState.moduleId == module.id

    if (showInputDialog) {
        ExpressiveDialog(
            onDismissRequest = {
                actions.onHideInputDialog()
            },
            title = { Text(stringResource(R.string.kpm_control)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.inputDialogState.args,
                        onValueChange = { actions.onInputArgsChange(it) },
                        label = { Text(stringResource(R.string.kpm_args)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (state.inputDialogState.args.isEmpty() && module.args.isNotEmpty()) {
                        Text(
                            text = module.args,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { actions.onExecuteControl() }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { actions.onHideInputDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    TonalCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp, 14.dp, 16.dp, 12.dp)
        ) {
            // FolkPatch label row: KPM tag + Args tag when the module carries args
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusTagMaterial(
                    label = "KPM",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (module.args.isNotBlank()) {
                    StatusTagMaterial(
                        label = stringResource(R.string.kpm_args),
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = module.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${stringResource(R.string.kpm_version)}: ${module.version}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${stringResource(R.string.kpm_author)}: ${module.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (module.args.isNotBlank()) {
                Text(
                    text = "${stringResource(R.string.kpm_args)}: ${module.args}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (module.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (module.hasAction) {
                    FilledTonalButton(
                        onClick = { actions.onShowInputDialog(module.id) },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.kpm_control),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.kpm_control))
                    }
                }

                Spacer(Modifier.weight(1f))

                FilledTonalButton(
                    onClick = onUninstall,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.kpm_uninstall))
                }
            }
        }
    }
}

@Composable
private fun EmptyStateViewMaterial() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                stringResource(R.string.kpm_empty),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
