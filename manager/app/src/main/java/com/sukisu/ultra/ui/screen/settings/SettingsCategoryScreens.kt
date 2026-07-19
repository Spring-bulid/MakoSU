package com.sukisu.ultra.ui.screen.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.RemoveModerator
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.KsuIsValid
import com.sukisu.ultra.ui.component.material.SegmentedColumn
import com.sukisu.ultra.ui.component.material.SegmentedDropdownItem
import com.sukisu.ultra.ui.component.material.SegmentedSwitchItem
import com.sukisu.ultra.ui.component.material.SendLogBottomSheet
import com.sukisu.ultra.ui.component.material.SplicedSettingsItem
import com.sukisu.ultra.ui.component.uninstalldialog.UninstallDialog
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.navigation3.Route
import com.sukisu.ultra.ui.util.getSuSFSStatus
import com.sukisu.ultra.ui.util.rememberKpmAvailable
import com.sukisu.ultra.ui.viewmodel.SettingsViewModel

@Composable
private fun rememberSettingsState(): Pair<SettingsUiState, SettingsViewModel> {
    val viewModel = viewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    return uiState to viewModel
}

@Composable
fun SettingsGeneralScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val (uiState, viewModel) = rememberSettingsState()

    SettingsCategoryScaffold(
        title = stringResource(R.string.settings_category_general),
        onBack = dropUnlessResumed { navigator.pop() },
    ) {
        KsuIsValid {
            SegmentedColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item(key = "check-update") {
                    SegmentedSwitchItem(
                        icon = Icons.Filled.Update,
                        title = stringResource(R.string.settings_check_update),
                        summary = stringResource(R.string.settings_check_update_summary),
                        checked = uiState.checkUpdate,
                        onCheckedChange = viewModel::setCheckUpdate,
                    )
                }
                item(key = "check-module-update") {
                    SegmentedSwitchItem(
                        icon = Icons.Rounded.UploadFile,
                        title = stringResource(R.string.settings_module_check_update),
                        summary = stringResource(R.string.settings_check_update_summary),
                        checked = uiState.checkModuleUpdate,
                        onCheckedChange = viewModel::setCheckModuleUpdate,
                    )
                }
            }
        }

        SegmentedColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            item(key = "alt-icon") {
                SegmentedSwitchItem(
                    icon = Icons.Rounded.Android,
                    title = stringResource(R.string.icon_switch_title),
                    summary = stringResource(R.string.icon_switch_summary),
                    checked = uiState.alternativeIcon,
                    onCheckedChange = { viewModel.setAlternativeIcon(context, it) },
                )
            }
        }

        KsuIsValid {
            SegmentedColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item(key = "profile-template") {
                    SplicedSettingsItem(
                        icon = Icons.Filled.Fence,
                        title = stringResource(R.string.settings_profile_template),
                        summary = stringResource(R.string.settings_profile_template_summary),
                        onClick = { navigator.push(Route.AppProfileTemplate) },
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsFeaturesScreen() {
    val navigator = LocalNavigator.current
    val (uiState, viewModel) = rememberSettingsState()
    val suCompatModeItems = listOf(
        stringResource(R.string.settings_mode_enable_by_default),
        stringResource(R.string.settings_mode_disable_until_reboot),
        stringResource(R.string.settings_mode_disable_always),
    )

    SettingsCategoryScaffold(
        title = stringResource(R.string.settings_category_features),
        onBack = dropUnlessResumed { navigator.pop() },
    ) {
        KsuIsValid {
            SegmentedColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item(key = "sucompat") {
                    val suSummary = when (uiState.suCompatStatus) {
                        "unsupported" -> stringResource(R.string.feature_status_unsupported_summary)
                        "managed" -> stringResource(R.string.feature_status_managed_summary)
                        else -> stringResource(R.string.settings_sucompat_summary)
                    }
                    SegmentedDropdownItem(
                        icon = Icons.Filled.RemoveModerator,
                        title = stringResource(R.string.settings_sucompat),
                        summary = suSummary,
                        items = suCompatModeItems,
                        enabled = uiState.suCompatStatus == "supported",
                        selectedIndex = uiState.suCompatMode,
                        onItemSelected = viewModel::setSuCompatMode,
                    )
                }
                item(key = "kernel-umount") {
                    val summary = when (uiState.kernelUmountStatus) {
                        "unsupported" -> stringResource(R.string.feature_status_unsupported_summary)
                        "managed" -> stringResource(R.string.feature_status_managed_summary)
                        else -> stringResource(R.string.settings_kernel_umount_summary)
                    }
                    SegmentedSwitchItem(
                        icon = Icons.Filled.RemoveCircle,
                        title = stringResource(R.string.settings_kernel_umount),
                        summary = summary,
                        enabled = uiState.kernelUmountStatus == "supported",
                        checked = uiState.isKernelUmountEnabled,
                        onCheckedChange = viewModel::setKernelUmountEnabled,
                    )
                }
                item(key = "selinux-hide") {
                    val summary = when (uiState.selinuxHideStatus) {
                        "unsupported" -> stringResource(R.string.feature_status_unsupported_summary)
                        "managed" -> stringResource(R.string.feature_status_managed_summary)
                        else -> stringResource(R.string.settings_selinux_hide_summary)
                    }
                    SegmentedSwitchItem(
                        icon = Icons.Filled.Policy,
                        title = stringResource(R.string.settings_selinux_hide),
                        summary = summary,
                        enabled = uiState.selinuxHideStatus == "supported",
                        checked = uiState.isSelinuxHideEnabled,
                        onCheckedChange = viewModel::setSelinuxHideEnabled,
                    )
                }
                item(key = "sulog") {
                    val summary = when (uiState.sulogStatus) {
                        "unsupported" -> stringResource(R.string.feature_status_unsupported_summary)
                        "managed" -> stringResource(R.string.feature_status_managed_summary)
                        else -> stringResource(R.string.settings_sulog_summary)
                    }
                    SegmentedSwitchItem(
                        icon = Icons.AutoMirrored.Filled.Article,
                        title = stringResource(R.string.settings_sulog),
                        summary = summary,
                        enabled = uiState.sulogStatus == "supported",
                        checked = uiState.isSulogEnabled,
                        onCheckedChange = viewModel::setSulogEnabled,
                    )
                }
                item(key = "adb-root") {
                    val summary = when (uiState.adbRootStatus) {
                        "unsupported" -> stringResource(R.string.feature_status_unsupported_summary)
                        "managed" -> stringResource(R.string.feature_status_managed_summary)
                        else -> stringResource(R.string.settings_adb_root_summary)
                    }
                    SegmentedSwitchItem(
                        icon = Icons.Filled.Adb,
                        title = stringResource(R.string.settings_adb_root),
                        summary = summary,
                        enabled = uiState.adbRootStatus == "supported",
                        checked = uiState.isAdbRootEnabled,
                        onCheckedChange = viewModel::setAdbRootEnabled,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsBehaviorScreen() {
    val navigator = LocalNavigator.current
    val (uiState, viewModel) = rememberSettingsState()

    SettingsCategoryScaffold(
        title = stringResource(R.string.settings_category_behavior),
        onBack = dropUnlessResumed { navigator.pop() },
    ) {
        KsuIsValid {
            SegmentedColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item(key = "umount-default") {
                    SegmentedSwitchItem(
                        icon = Icons.Filled.FolderDelete,
                        title = stringResource(R.string.settings_umount_modules_default),
                        summary = stringResource(R.string.settings_umount_modules_default_summary),
                        checked = uiState.isDefaultUmountModules,
                        onCheckedChange = viewModel::setDefaultUmountModules,
                    )
                }
                item(key = "web-debug") {
                    SegmentedSwitchItem(
                        icon = Icons.Filled.DeveloperMode,
                        title = stringResource(R.string.enable_web_debugging),
                        summary = stringResource(R.string.enable_web_debugging_summary),
                        checked = uiState.enableWebDebugging,
                        onCheckedChange = viewModel::setEnableWebDebugging,
                    )
                }
                item(key = "auto-jailbreak") {
                    SegmentedSwitchItem(
                        icon = Icons.Filled.ElectricalServices,
                        title = stringResource(R.string.settings_auto_jailbreak),
                        summary = stringResource(R.string.settings_auto_jailbreak_summary),
                        enabled = uiState.isLateLoadMode,
                        checked = uiState.autoJailbreak,
                        onCheckedChange = viewModel::setAutoJailbreak,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsModuleScreen() {
    val navigator = LocalNavigator.current
    val isKpmAvailable = rememberKpmAvailable()
    val isSusfsSupported = getSuSFSStatus().equals("true", ignoreCase = true)

    SettingsCategoryScaffold(
        title = stringResource(R.string.settings_category_module),
        onBack = dropUnlessResumed { navigator.pop() },
    ) {
        KsuIsValid {
            SegmentedColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item(key = "tools") {
                    SplicedSettingsItem(
                        icon = Icons.Filled.Fence,
                        title = stringResource(R.string.settings_tools),
                        summary = stringResource(R.string.settings_tools_summary),
                        onClick = { navigator.push(Route.Tool) },
                    )
                }
                item(key = "kpm", visible = isKpmAvailable) {
                    SplicedSettingsItem(
                        icon = Icons.Filled.Fence,
                        title = stringResource(R.string.kpm_title),
                        summary = stringResource(R.string.settings_kpm_summary),
                        onClick = { navigator.push(Route.Kpm) },
                    )
                }
                item(key = "susfs", visible = isSusfsSupported && isKpmAvailable) {
                    SplicedSettingsItem(
                        icon = Icons.Filled.Fence,
                        title = stringResource(R.string.susfs_config_title),
                        summary = stringResource(R.string.settings_kpm_summary),
                        onClick = { navigator.push(Route.SuSFS) },
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsMoreScreen() {
    val navigator = LocalNavigator.current
    val (uiState, _) = rememberSettingsState()
    val snackBarHost = remember { SnackbarHostState() }
    val showUninstallDialog = rememberSaveable { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    UninstallDialog(
        show = showUninstallDialog.value,
        onDismissRequest = { showUninstallDialog.value = false },
    )

    SettingsCategoryScaffold(
        title = stringResource(R.string.settings_category_more),
        onBack = dropUnlessResumed { navigator.pop() },
    ) {
        if (uiState.isLkmMode) {
            SegmentedColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                item(key = "uninstall") {
                    SplicedSettingsItem(
                        icon = Icons.Filled.Delete,
                        title = stringResource(R.string.settings_uninstall),
                        onClick = { showUninstallDialog.value = true },
                        enabled = !uiState.isLateLoadMode,
                        showArrow = false,
                    )
                }
            }
        }

        SegmentedColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            item(key = "send-log") {
                SplicedSettingsItem(
                    icon = Icons.Filled.BugReport,
                    title = stringResource(R.string.send_log),
                    onClick = { showBottomSheet = true },
                    showArrow = false,
                )
            }
            item(key = "about") {
                SplicedSettingsItem(
                    icon = Icons.Filled.ContactPage,
                    title = stringResource(R.string.about),
                    onClick = { navigator.push(Route.About) },
                )
            }
        }

        if (showBottomSheet) {
            SendLogBottomSheet(
                onDismiss = { showBottomSheet = false },
                snackbarHostState = snackBarHost,
            )
        }
    }
}
