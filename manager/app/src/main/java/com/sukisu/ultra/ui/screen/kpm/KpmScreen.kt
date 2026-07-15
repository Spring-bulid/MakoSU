package com.sukisu.ultra.ui.screen.kpm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.LocalUiMode
import com.sukisu.ultra.ui.UiMode
import com.sukisu.ultra.ui.component.dialog.ConfirmResult
import com.sukisu.ultra.ui.component.dialog.rememberConfirmDialog
import com.sukisu.ultra.ui.util.getRootShell
import com.sukisu.ultra.ui.util.loadKpmModule
import com.sukisu.ultra.ui.util.unloadKpmModule
import com.sukisu.ultra.ui.viewmodel.KpmViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLEncoder
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun KpmScreen(
    bottomInnerPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<KpmViewModel>()
    val confirmDialog = rememberConfirmDialog()

    val kpmInstallSuccess = stringResource(R.string.kpm_install_success)
    val kpmInstallFailed = stringResource(R.string.kpm_install_failed)
    val kpmUninstallSuccess = stringResource(R.string.kpm_uninstall_success)
    val kpmUninstallFailed = stringResource(R.string.kpm_uninstall_failed)
    val invalidFileTypeMessage = stringResource(R.string.invalid_file_type)
    val failedToCheckModuleFile = stringResource(R.string.snackbar_failed_to_check_module_file)

    val showToast: suspend (String) -> Unit = { msg ->
        withContext(Dispatchers.Main.immediate) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val selectPatchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        val uri = result.data?.data ?: return@rememberLauncherForActivityResult

        scope.launch(Dispatchers.IO) {
            val fileName = uri.lastPathSegment ?: "unknown.kpm"
            val encodedFileName = URLEncoder.encode(fileName, "UTF-8")
            val tempFile = File(context.cacheDir, encodedFileName)

            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: 0L

            if (copied <= 0L || !isValidKpmFile(tempFile, context.contentResolver.getType(uri))) {
                showToast(invalidFileTypeMessage)
                tempFile.delete()
                return@launch
            }

            val moduleName = extractModuleName(tempFile)
            withContext(Dispatchers.Main) {
                viewModel.showInstallDialog(moduleName ?: tempFile.nameWithoutExtension)
                viewModel.setTempFileForInstall(tempFile)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchModuleList()
    }

    val actions = KpmActions(
        onRefresh = { viewModel.fetchModuleList() },
        onSearchStatusChange = viewModel::updateSearchStatus,
        onSearchTextChange = { text ->
            scope.launch { viewModel.updateSearchText(text) }
        },
        onRequestInstall = {
            selectPatchLauncher.launch(
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/octet-stream"
                }
            )
        },
        onConfirmInstall = { _, isEmbed ->
            scope.launch(Dispatchers.IO) {
                val tempFile = viewModel.getTempFile()
                tempFile?.let {
                    handleModuleInstall(
                        tempFilePath = it.absolutePath,
                        isEmbed = isEmbed,
                        viewModel = viewModel,
                        showToast = showToast,
                        kpmInstallSuccess = kpmInstallSuccess,
                        kpmInstallFailed = kpmInstallFailed
                    )
                }
            }
        },
        onDismissInstallDialog = {
            viewModel.dismissInstallDialog()
            viewModel.clearTempFile()
        },
        onRequestUninstall = { moduleId ->
            scope.launch {
                val confirmContent = context.getString(R.string.confirm_uninstall_content, moduleId)
                val confirmResult = confirmDialog.awaitConfirm(
                    title = context.getString(R.string.confirm_uninstall_title_with_filename),
                    content = confirmContent,
                    confirm = context.getString(R.string.uninstall),
                    dismiss = context.getString(R.string.cancel)
                )

                if (confirmResult == ConfirmResult.Confirmed) {
                    withContext(Dispatchers.IO) {
                        handleModuleUninstall(
                            moduleId = moduleId,
                            showToast = showToast,
                            kpmUninstallSuccess = kpmUninstallSuccess,
                            kpmUninstallFailed = kpmUninstallFailed,
                            failedToCheckModuleFile = failedToCheckModuleFile,
                            viewModel = viewModel
                        )
                    }
                }
            }
        },
        onConfirmUninstall = { },
        onDismissUninstallDialog = { },
        onShowInputDialog = viewModel::showInputDialog,
        onHideInputDialog = viewModel::hideInputDialog,
        onInputArgsChange = viewModel::updateInputArgs,
        onExecuteControl = {
            scope.launch(Dispatchers.IO) {
                val result = viewModel.executeControl()
                val message = when (result) {
                    0 -> context.getString(R.string.kpm_control_success)
                    else -> context.getString(R.string.kpm_control_failed)
                }
                showToast(message)
            }
        },
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> KpmMiuix(
            viewModel = viewModel,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding
        )
        UiMode.Material -> KpmMaterial(
            viewModel = viewModel,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding
        )
    }
}

private suspend fun handleModuleInstall(
    tempFilePath: String,
    isEmbed: Boolean,
    viewModel: KpmViewModel,
    showToast: suspend (String) -> Unit,
    kpmInstallSuccess: String,
    kpmInstallFailed: String
) {
    val tempFile = File(tempFilePath)
    val moduleId = extractModuleName(tempFile)
    if (moduleId.isNullOrEmpty()) {
        showToast(kpmInstallFailed)
        tempFile.delete()
        return
    }

    val targetPath = "/data/adb/kpm/$moduleId.kpm"

    try {
        if (isEmbed) {
            val shell = getRootShell()
            shell.newJob().add("mkdir -p /data/adb/kpm").exec()
            shell.newJob().add("cp ${tempFile.absolutePath} $targetPath").exec()
        }

        val loadResult = loadKpmModule(tempFile.absolutePath)
        if (!loadResult) {
            showToast(kpmInstallFailed)
        } else {
            viewModel.fetchModuleList()
            showToast(kpmInstallSuccess)
        }
    } catch (_: Exception) {
        showToast(kpmInstallFailed)
    }
    tempFile.delete()
    viewModel.dismissInstallDialog()
    viewModel.clearTempFile()
}

private suspend fun handleModuleUninstall(
    moduleId: String,
    showToast: suspend (String) -> Unit,
    kpmUninstallSuccess: String,
    kpmUninstallFailed: String,
    failedToCheckModuleFile: String,
    viewModel: KpmViewModel
) {
    val moduleFileName = "$moduleId.kpm"
    val moduleFilePath = "/data/adb/kpm/$moduleFileName"

    val fileExists = try {
        val shell = getRootShell()
        val result = shell.newJob().add("ls /data/adb/kpm/$moduleFileName").exec()
        result.isSuccess
    } catch (_: Exception) {
        showToast(failedToCheckModuleFile)
        false
    }

    try {
        val unloadResult = unloadKpmModule(moduleId)
        if (!unloadResult) {
            showToast(kpmUninstallFailed)
            return
        }

        if (fileExists) {
            val shell = getRootShell()
            shell.newJob().add("rm $moduleFilePath").exec()
        }

        viewModel.fetchModuleList()
        showToast(kpmUninstallSuccess)
    } catch (_: Exception) {
        showToast(kpmUninstallFailed)
    }
}
