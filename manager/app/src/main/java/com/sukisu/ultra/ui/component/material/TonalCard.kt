package com.sukisu.ultra.ui.component.material

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled

/**
 * FolkPatch-aligned tonal surface card.
 * Defaults match FolkPatch HomeCircle.TonalCard:
 * - shape: RoundedCornerShape(20.dp)
 * - color: surfaceColorAtElevation(1.dp) (surface + opacity when wallpaper)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TonalCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val resolvedContainerColor = containerColor ?: if (LocalCustomBackgroundEnabled.current) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }
    val resolvedContentColor = contentColor ?: contentColorFor(resolvedContainerColor)
    val colors = CardDefaults.cardColors(
        containerColor = resolvedContainerColor,
        contentColor = resolvedContentColor,
    )
    when {
        onLongClick != null -> Card(
            modifier = modifier
                .clip(shape)
                .combinedClickable(
                    enabled = enabled,
                    onClick = onClick ?: {},
                    onLongClick = onLongClick,
                ),
            colors = colors,
            shape = shape,
        ) { content() }

        onClick != null -> Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            shape = shape,
        ) { content() }

        else -> Card(
            modifier = modifier,
            colors = colors,
            shape = shape,
        ) { content() }
    }
}
