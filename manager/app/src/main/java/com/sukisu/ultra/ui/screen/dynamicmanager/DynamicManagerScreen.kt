package com.sukisu.ultra.ui.screen.dynamicmanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.AppIconImage
import com.sukisu.ultra.ui.component.material.ExpressiveConfirmDialog
import com.sukisu.ultra.ui.component.material.ExpressiveDialog
import com.sukisu.ultra.ui.component.material.ExpressiveScaffold
import com.sukisu.ultra.ui.component.material.SearchAppBar
import com.sukisu.ultra.ui.component.material.SegmentedColumn
import com.sukisu.ultra.ui.component.material.SegmentedItem
import com.sukisu.ultra.ui.component.material.SegmentedListItem
import com.sukisu.ultra.ui.component.material.SnackBarHost
import com.sukisu.ultra.ui.component.material.SplicedSettingsItem
import com.sukisu.ultra.ui.component.material.TopBarBackButton
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.viewmodel.DynamicManagerAppItem
import com.sukisu.ultra.ui.viewmodel.DynamicManagerUiState
import com.sukisu.ultra.ui.viewmodel.DynamicManagerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DynamicManagerScreen() {
    val navigator = LocalNavigator.current
    val viewModel = viewModel<DynamicManagerViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val pullToRefreshState = rememberPullToRefreshState()
    val snackBarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val setSuccess = stringResource(R.string.dynamic_manager_set_success)
    val setFailed = stringResource(R.string.dynamic_manager_set_failed)
    val clearSuccess = stringResource(R.string.dynamic_manager_disabled_success)
    val clearFailed = stringResource(R.string.dynamic_manager_clear_failed)

    var showManualDialog by rememberSaveable { mutableStateOf(false) }
    var showClearConfirm by rememberSaveable { mutableStateOf(false) }
    var pendingGrant by remember { mutableStateOf<(suspend () -> Boolean)?>(null) }

    fun reportResult(success: Boolean, successMsg: String, failedMsg: String) {
        scope.launch {
            if (success) viewModel.refresh()
            snackBarHost.showSnackbar(if (success) successMsg else failedMsg)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    if (showClearConfirm) {
        ExpressiveConfirmDialog(
            title = stringResource(R.string.dynamic_manager_clear_confirm_title),
            message = stringResource(R.string.dynamic_manager_clear_confirm_message),
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(android.R.string.cancel),
            onConfirm = {
                showClearConfirm = false
                scope.launch {
                    reportResult(viewModel.clearConfig(), clearSuccess, clearFailed)
                }
            },
            onDismiss = { showClearConfirm = false },
        )
    }

    pendingGrant?.let { operation ->
        ExpressiveConfirmDialog(
            title = stringResource(R.string.dynamic_manager_grant_confirm_title),
            message = stringResource(R.string.dynamic_manager_grant_confirm_message),
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(android.R.string.cancel),
            onConfirm = {
                pendingGrant = null
                scope.launch {
                    reportResult(operation(), setSuccess, setFailed)
                }
            },
            onDismiss = { pendingGrant = null },
        )
    }

    if (showManualDialog) {
        DynamicManagerManualDialog(
            onDismiss = { showManualDialog = false },
            onConfirm = { size, hash ->
                showManualDialog = false
                pendingGrant = { viewModel.setManualConfig(size, hash) }
            },
        )
    }

    ExpressiveScaffold(
        topBar = {
            SearchAppBar(
                title = { Text(stringResource(R.string.dynamic_manager_title)) },
                searchText = uiState.search,
                onSearchTextChange = viewModel::updateSearch,
                onClearClick = { viewModel.updateSearch("") },
                navigationIcon = {
                    TopBarBackButton(onClick = dropUnlessResumed { navigator.pop() })
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackBarHost(hostState = snackBarHost) },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        ),
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator()
            }
        } else {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = uiState.isRefreshing,
                onRefresh = {
                    scope.launch { viewModel.refresh() }
                },
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = pullToRefreshState,
                        isRefreshing = uiState.isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 5.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    ),
                ) {
                    item(key = "status") {
                        DynamicManagerStatusSection(
                            uiState = uiState,
                            enabled = !uiState.isSubmitting,
                            onManualConfig = { showManualDialog = true },
                            onClearConfig = { showClearConfirm = true },
                        )
                    }

                    item(key = "managers-title") {
                        Text(
                            text = stringResource(R.string.dynamic_manager_manage_managers),
                            style = MaterialTheme.typography.titleSmall,
                            color = colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
                        )
                    }

                    if (uiState.apps.isEmpty()) {
                        item(key = "empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.dynamic_manager_no_candidates),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            uiState.apps,
                            key = { _, app -> "${app.uid}-${app.packageName}" },
                            contentType = { _, _ -> "DynamicManagerAppItem" },
                        ) { index, app ->
                            SegmentedItem(index = index, count = uiState.apps.size) {
                                DynamicManagerAppRow(
                                    app = app,
                                    onClick = {
                                        if (app.isSelected) {
                                            showClearConfirm = true
                                        } else {
                                            pendingGrant = { viewModel.setManagerApp(app) }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicManagerStatusSection(
    uiState: DynamicManagerUiState,
    enabled: Boolean,
    onManualConfig: () -> Unit,
    onClearConfig: () -> Unit,
) {
    val config = uiState.config
    val currentStatus = if (config?.isValid() == true) {
        stringResource(R.string.dynamic_manager_enabled_summary, config.size.toString())
    } else {
        stringResource(R.string.dynamic_manager_disabled)
    }

    SegmentedColumn(
        title = stringResource(R.string.dynamic_manager_title),
    ) {
        item(key = "status") {
            SplicedSettingsItem(
                icon = Icons.Filled.Security,
                title = stringResource(R.string.dynamic_manager_current_status),
                summary = currentStatus,
                showArrow = false,
            )
        }

        item(key = "hash", visible = config?.isValid() == true) {
            SplicedSettingsItem(
                icon = Icons.Filled.Fingerprint,
                title = stringResource(R.string.dynamic_manager_signature_hash),
                summary = config?.hash.orEmpty(),
                showArrow = false,
            )
        }

        item(key = "manual") {
            SplicedSettingsItem(
                icon = Icons.Filled.Edit,
                title = stringResource(R.string.dynamic_manager_manual_config),
                summary = stringResource(R.string.dynamic_manager_manual_config_summary),
                enabled = enabled,
                onClick = onManualConfig,
            )
        }

        item(key = "clear") {
            SplicedSettingsItem(
                icon = Icons.Filled.Delete,
                title = stringResource(R.string.dynamic_manager_clear_config),
                summary = stringResource(R.string.dynamic_manager_clear_config_summary),
                enabled = enabled,
                onClick = onClearConfig,
            )
        }
    }
}

@Composable
private fun DynamicManagerAppRow(
    app: DynamicManagerAppItem,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        enabled = app.isChangeable,
        onClick = onClick,
        headlineContent = {
            Text(
                text = app.label,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        supportingContent = {
            Text(
                text = if (!app.isChangeable) {
                    stringResource(
                        R.string.dynamic_manager_fixed_manager_summary,
                        app.packageName,
                        when (app.managerSignatureIndex ?: 0) {
                            254 -> "Debug"
                            253 -> "KernelSU Toolkit"
                            else -> "Kernel"
                        }
                    )
                } else {
                    app.packageName
                },
                color = colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = {
            AppIconImage(
                packageInfo = app.packageInfo,
                label = app.label,
                modifier = Modifier.size(48.dp),
            )
        },
        trailingContent = {
            Checkbox(
                checked = app.isSelected || !app.isChangeable,
                enabled = app.isChangeable,
                onCheckedChange = null,
            )
        },
    )
}

@Composable
private fun DynamicManagerManualDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit,
) {
    var size by rememberSaveable { mutableStateOf("") }
    var hash by rememberSaveable { mutableStateOf("") }
    val sizeValue = size.toIntOrNull()
    val hashValid = hash.length == 64
    val isValid = sizeValue != null && sizeValue > 0 && hashValid

    ExpressiveDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dynamic_manager_manual_config)) },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = size,
                    onValueChange = { size = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.dynamic_manager_signature_size)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = hash,
                    onValueChange = { hash = it.trim() },
                    label = { Text(stringResource(R.string.dynamic_manager_signature_hash)) },
                    isError = hash.isNotEmpty() && !hashValid,
                    supportingText = {
                        if (hash.isNotEmpty() && !hashValid) {
                            Text(stringResource(R.string.dynamic_manager_hash_must_be_64_chars))
                        }
                    },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(sizeValue ?: return@TextButton, hash) },
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
