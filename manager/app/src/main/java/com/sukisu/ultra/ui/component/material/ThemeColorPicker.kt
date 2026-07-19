package com.sukisu.ultra.ui.component.material

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.theme.keyColorOptions

/**
 * FolkPatch-style ThemeColorPicker.
 * Uses plain seed colors for swatches — never builds full dynamic schemes per swatch
 * (that previously OOM/crashed when opening Appearance).
 */
@Composable
fun ThemeColorPicker(
    selectedKeyColor: Int,
    onColorSelected: (Int) -> Unit,
    isDarkTheme: Boolean,
    isAmoled: Boolean,
    modifier: Modifier = Modifier,
    paletteStyle: Any? = null,
    colorSpec: Any? = null,
) {
    // paletteStyle/colorSpec kept for call-site compatibility; swatches only need seed ARGB.
    @Suppress("UNUSED_PARAMETER")
    val unusedStyle = paletteStyle
    @Suppress("UNUSED_PARAMETER")
    val unusedSpec = colorSpec
    @Suppress("UNUSED_PARAMETER")
    val unusedDark = isDarkTheme
    @Suppress("UNUSED_PARAMETER")
    val unusedAmoled = isAmoled

    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    TonalCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_key_color),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                if (dynamicSupported) {
                    item(key = "system_dynamic") {
                        ThemeColorCircle(
                            displayColor = MaterialTheme.colorScheme.primary,
                            isSelected = selectedKeyColor == 0,
                            isDynamic = true,
                            onClick = { onColorSelected(0) },
                        )
                    }
                }
                items(keyColorOptions, key = { it }) { colorArgb ->
                    ThemeColorCircle(
                        displayColor = Color(colorArgb),
                        isSelected = selectedKeyColor == colorArgb,
                        isDynamic = false,
                        onClick = { onColorSelected(colorArgb) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeColorCircle(
    displayColor: Color,
    isSelected: Boolean,
    isDynamic: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "colorScale",
    )
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(displayColor)
                .then(
                    if (isDynamic) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White,
                )
            }
        }
    }
}
