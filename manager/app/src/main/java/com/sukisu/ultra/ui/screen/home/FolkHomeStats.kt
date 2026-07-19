package com.sukisu.ultra.ui.screen.home

/**
 * StatsUI / PureUI home content (FolkPatch Stats base):
 * - StatsUI: Working card + compact module-stats card + system info
 * - PureUI: same as StatsUI without the module-stats card
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R

@Composable
internal fun FolkHomeScreenStats(
    state: HomeUiState,
    actions: HomeActions,
) {
    StatsHomeContent(
        state = state,
        actions = actions,
        showModuleStats = true,
    )
}

/** PureUI = Stats without the module-stats card (next after working card). */
@Composable
internal fun HomeScreenPure(
    state: HomeUiState,
    actions: HomeActions,
) {
    StatsHomeContent(
        state = state,
        actions = actions,
        showModuleStats = false,
    )
}

@Composable
private fun StatsHomeContent(
    state: HomeUiState,
    actions: HomeActions,
    showModuleStats: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 工作中卡片
        StatusCardCircle(state = state, actions = actions)

        // 工作中下一张：SU / 模块统计卡（仅 StatsUI；并缩小）
        if (showModuleStats && state.ksuVersion != null) {
            ModuleStatisticsSection(
                superuserCount = state.superuserCount,
                moduleCount = state.moduleCount,
                onSuperuserClick = actions.onSuperuserClick,
                onModuleClick = actions.onModuleClick,
            )
        }

        ListInfoCard(state = state)
        LearnMoreCard(onOpenUrl = actions.onOpenUrl)
    }
}

/** Compact module statistics card (after working status). */
@Composable
private fun ModuleStatisticsSection(
    superuserCount: Int,
    moduleCount: Int,
    onSuperuserClick: () -> Unit,
    onModuleClick: () -> Unit,
) {
    FolkTonalCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val total = superuserCount + moduleCount
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    strokeWidth = 8.dp,
                )
                if (total > 0) {
                    CircularProgressIndicator(
                        progress = { superuserCount.toFloat() / total },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 8.dp,
                        trackColor = Color.Transparent,
                    )
                }
                Text(
                    text = if (total > 0) total.toString() else "--",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f),
            ) {
                StatRow(
                    label = stringResource(R.string.superuser),
                    value = superuserCount.toString(),
                    icon = Icons.Outlined.Security,
                    onClick = onSuperuserClick,
                )
                StatRow(
                    label = stringResource(R.string.module),
                    value = moduleCount.toString(),
                    icon = Icons.Outlined.Extension,
                    onClick = onModuleClick,
                )
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
