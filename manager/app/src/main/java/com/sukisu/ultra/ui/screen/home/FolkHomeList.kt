package com.sukisu.ultra.ui.screen.home

/**
 * ListUI home content (FolkPatch Home.kt V1 / KStatusCard base):
 * filled status banner (primary when working) with mode StatusBadge,
 * then list info card and learn-more card.
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled

@Composable
internal fun FolkHomeScreenList(
    state: HomeUiState,
    actions: HomeActions,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusCardList(state = state, actions = actions)
        ListInfoCard(state = state)
        LearnMoreCard(onOpenUrl = actions.onOpenUrl)
    }
}

/** FolkPatch KStatusCard — filled primary banner when working, tonal surface otherwise. */
@Composable
private fun StatusCardList(
    state: HomeUiState,
    actions: HomeActions,
) {
    val working = isWorking(state)
    val customBg = LocalCustomBackgroundEnabled.current
    val containerColor = if (working) {
        if (customBg) MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
        else MaterialTheme.colorScheme.primary
    } else {
        if (customBg) MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.72f)
        else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }
    val contentColor = if (working) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val badge = modeBadge(state)

    FolkTonalCard(containerColor = containerColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!working && !state.isLateLoadMode) actions.onInstallClick()
                }
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (working) Icons.Filled.CheckCircle else Icons.Outlined.Warning,
                contentDescription = null,
                tint = contentColor,
            )
            Column(Modifier.padding(start = 16.dp)) {
                if (working) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.home_working),
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                        )
                        if (badge != null) {
                            Spacer(Modifier.width(8.dp))
                            StatusBadge(text = badge)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_working_version, ksuVersionLabel(state)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                    )
                } else {
                    Text(
                        text = if (isSupported(state)) {
                            stringResource(R.string.home_not_installed)
                        } else {
                            stringResource(R.string.home_unsupported)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isSupported(state)) {
                            stringResource(R.string.home_click_to_install)
                        } else {
                            stringResource(R.string.home_unsupported_reason)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
