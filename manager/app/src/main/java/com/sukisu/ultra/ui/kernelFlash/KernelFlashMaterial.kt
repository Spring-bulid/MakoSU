package com.sukisu.ultra.ui.kernelFlash

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.component.KeyEventBlocker
import com.sukisu.ultra.ui.kernelFlash.state.FlashState

@Composable
fun KernelFlashMaterial(
    state: FlashState,
    actions: KernelFlashActions,
    logText: String,
    kpmPatchEnabled: Boolean,
    kpmUndoPatch: Boolean
) {
    val scrollState = rememberScrollState()

    BackHandler {
        actions.onBack()
    }

    KeyEventBlocker {
        it.key == Key.VolumeDown || it.key == Key.VolumeUp
    }

    DisposableEffect(Unit) {
        onDispose {
            if (state.isCompleted || state.error.isNotEmpty()) {
                KernelFlashStateHolder.clear()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            when {
                                state.error.isNotEmpty() -> R.string.flash_failed
                                state.isCompleted -> R.string.flash_success
                                else -> R.string.kernel_flashing
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { actions.onSaveLog(logText) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.save_log)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (state.isCompleted) {
                FloatingActionButton(
                    onClick = actions.onReboot,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 20.dp, end = 20.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.reboot),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FlashProgressIndicatorMaterial(state, kpmPatchEnabled, kpmUndoPatch)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                LaunchedEffect(logText) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = logText,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FlashProgressIndicatorMaterial(
    flashState: FlashState,
    kpmPatchEnabled: Boolean,
    kpmUndoPatch: Boolean
) {
    val statusColor = when {
        flashState.error.isNotEmpty() -> MaterialTheme.colorScheme.error
        flashState.isCompleted -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    val progress = animateFloatAsState(
        targetValue = flashState.progress.coerceIn(0f, 1f),
        label = "FlashProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when {
                        flashState.error.isNotEmpty() -> stringResource(R.string.flash_failed)
                        flashState.isCompleted -> stringResource(R.string.flash_success)
                        else -> stringResource(R.string.flashing)
                    },
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )

                when {
                    flashState.error.isNotEmpty() -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    flashState.isCompleted -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (kpmPatchEnabled || kpmUndoPatch) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (kpmUndoPatch) stringResource(R.string.kpm_undo_patch_mode)
                    else stringResource(R.string.kpm_patch_mode),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (flashState.currentStep.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = flashState.currentStep,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (flashState.error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = flashState.error,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.shapes.small
                        )
                        .padding(12.dp)
                )
            }
        }
    }
}
