package com.sukisu.ultra.ui.screen.home

/**
 * StatusCardCircle shared by StatsUI (FolkPatch circle status).
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled

/** FolkPatch StatusCardCircle — used by StatsUI home layout. */
@Composable
internal fun StatusCardCircle(
    state: HomeUiState,
    actions: HomeActions,
) {
    val working = isWorking(state)
    val finalContainerColor = if (working) {
        if (LocalCustomBackgroundEnabled.current) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    } else {
        if (LocalCustomBackgroundEnabled.current) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    }
    val badge = modeBadge(state)

    FolkTonalCard(containerColor = finalContainerColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!state.isLateLoadMode) actions.onInstallClick()
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (working) {
                Icon(Icons.Outlined.CheckCircle, stringResource(R.string.home_working))
                Column(Modifier.padding(start = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.home_working),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (badge != null) {
                            Spacer(Modifier.width(8.dp))
                            ModeLabelText(label = badge)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_working_version, ksuVersionLabel(state)),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Icon(Icons.Outlined.Warning, stringResource(R.string.home_not_installed))
                Column(Modifier.padding(start = 20.dp)) {
                    Text(
                        text = if (isSupported(state)) {
                            stringResource(R.string.home_not_installed)
                        } else {
                            stringResource(R.string.home_unsupported)
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isSupported(state)) {
                            stringResource(R.string.home_click_to_install)
                        } else {
                            stringResource(R.string.home_unsupported_reason)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
