package com.sukisu.ultra.ui.component.material

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.theme.ColorMode

/**
 * FolkPatch ThemeModeSelector: Light / Dark / System.
 * Maps to MakoSU [ColorMode] (keeps AMOLED via separate switch).
 */
@Composable
fun ThemeModeSelector(
    selectedMode: ColorMode,
    onModeSelected: (ColorMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Normalize Monet / AMOLED modes into the 3 FolkPatch buckets
    val bucket = when (selectedMode) {
        ColorMode.LIGHT, ColorMode.MONET_LIGHT -> ColorMode.LIGHT
        ColorMode.DARK, ColorMode.MONET_DARK, ColorMode.DARK_AMOLED -> ColorMode.DARK
        else -> ColorMode.SYSTEM
    }

    TonalCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemeModeOption(
                icon = Icons.Filled.LightMode,
                label = stringResource(R.string.settings_theme_mode_light),
                isSelected = bucket == ColorMode.LIGHT,
                onClick = { onModeSelected(ColorMode.LIGHT) },
                modifier = Modifier.weight(1f),
            )
            ThemeModeOption(
                icon = Icons.Filled.DarkMode,
                label = stringResource(R.string.settings_theme_mode_dark),
                isSelected = bucket == ColorMode.DARK,
                onClick = { onModeSelected(ColorMode.DARK) },
                modifier = Modifier.weight(1f),
            )
            ThemeModeOption(
                icon = Icons.Filled.AutoAwesome,
                label = stringResource(R.string.settings_theme_mode_system),
                isSelected = bucket == ColorMode.SYSTEM,
                onClick = { onModeSelected(ColorMode.SYSTEM) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "themeModeScale",
    )
    val bg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = contentColor)
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}
