package com.sukisu.ultra.ui.screen.flash

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.sukisu.ultra.Natives
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.util.reboot
import kotlinx.coroutines.delay

@Composable
fun FlashScreen(flashIt: FlashIt) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf("") }
    val logContent = remember { StringBuilder() }
    val snackBarHost = remember { SnackbarHostState() }
    var showRebootAction by rememberSaveable { mutableStateOf(false) }
    var flashingStatus by rememberSaveable { mutableStateOf(FlashingStatus.FLASHING) }
    val needJailbreakWarning = flashIt is FlashIt.FlashBoot && Natives.isLateLoadMode
    var flashingEnabled by rememberSaveable { mutableStateOf(!needJailbreakWarning) }

    fun showMessage(message: String) {
        scope.launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    FlashEffect(
        flashIt = flashIt,
        text = text,
        logContent = logContent,
        onTextUpdate = { text = it },
        onShowRebootChange = { showRebootAction = it },
        onFlashingStatusChange = { flashingStatus = it },
        enabled = flashingEnabled,
    )

    LaunchedEffect(flashingStatus, flashIt) {
        if (flashingStatus == FlashingStatus.SUCCESS && flashIt is FlashIt.FlashModules) {
            val intent = activity?.intent
            val isFromExternalIntent = intent?.action?.let { action ->
                action == Intent.ACTION_VIEW ||
                        action == Intent.ACTION_SEND ||
                        action == Intent.ACTION_SEND_MULTIPLE
            } ?: false

            if (isFromExternalIntent) {
                delay(1000)
                activity.finish()
            }
        }
    }

    val state = FlashUiState(
        text = text,
        showRebootAction = showRebootAction,
        flashingStatus = flashingStatus,
        showJailbreakWarning = needJailbreakWarning && !flashingEnabled,
    )
    val actions = FlashScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSaveLog = saveLog(logContent, scope) { showMessage(it) },
        onReboot = {
            scope.launch {
                withContext(Dispatchers.IO) {
                    reboot()
                }
            }
        },
        onConfirmJailbreakWarning = { flashingEnabled = true },
        onDismissJailbreakWarning = dropUnlessResumed { navigator.pop() },
    )

    FlashScreenMaterial(state, actions, snackBarHost)
}
