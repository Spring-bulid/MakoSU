package com.sukisu.ultra.ui.component.bottombar

/**
 * MakoSU navigation bar — FolkPatch floating-dock grammar, MakoSU identity.
 *
 * Floating (default):
 *  - Centered capsule over content (not full-width chrome)
 *  - Sliding circular indicator (spring)
 *  - Circular icon hit targets + circular ripple
 *  - primaryContainer indicator (MakoSU brand; FolkPatch uses secondaryContainer)
 *  - Selected icon scales slightly; optional selected label under icon row height
 *
 * Bottom:
 *  - Material3 NavigationBar with labels on selection
 */

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sukisu.ultra.Natives
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.LocalMainPagerState
import com.sukisu.ultra.ui.theme.LocalCustomBackgroundEnabled
import com.sukisu.ultra.ui.util.rootAvailable

private data class NavItem(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val mainNavItems = listOf(
    NavItem(R.string.home, Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(R.string.superuser, Icons.Filled.Shield, Icons.Outlined.Shield),
    NavItem(R.string.module, Icons.Filled.Extension, Icons.Outlined.Extension),
    NavItem(R.string.settings, Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun BottomBarMaterial(
    modifier: Modifier = Modifier,
    isFloating: Boolean = true,
    onUserInteraction: (() -> Unit)? = null,
) {
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    if (!fullFeatured) return

    if (isFloating) {
        MakoFloatingDock(modifier = modifier, onUserInteraction = onUserInteraction)
    } else {
        MakoStandardBar(modifier = modifier, onUserInteraction = onUserInteraction)
    }
}

/**
 * Floating capsule dock — FolkPatch layout grammar, MakoSU branding.
 */
@Composable
private fun MakoFloatingDock(
    modifier: Modifier = Modifier,
    onUserInteraction: (() -> Unit)? = null,
) {
    val mainPagerState = LocalMainPagerState.current
    val hasCustomBackground = LocalCustomBackgroundEnabled.current
    val items = mainNavItems

    // Metrics close to FolkPatch BottomBarContent, slightly tighter for MakoSU
    val itemSize = 52.dp
    val itemGap = 6.dp
    val containerPadding = 8.dp
    val barHeight = 68.dp
    val capsuleShape = RoundedCornerShape(percent = 50)

    val selectedPosition by animateFloatAsState(
        targetValue = mainPagerState.selectedPage.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "mako_dock_selection",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
    ) {
        val horizontalPad = when {
            maxWidth > 600.dp -> 32.dp
            maxWidth > 400.dp -> 24.dp
            else -> 16.dp
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPad, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val barColor = if (hasCustomBackground) {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }

            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .then(
                        if (hasCustomBackground) {
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                shape = capsuleShape,
                            )
                        } else {
                            Modifier
                        },
                    ),
                shape = capsuleShape,
                color = barColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = if (hasCustomBackground) 0.dp else 2.dp,
                shadowElevation = if (hasCustomBackground) 0.dp else 10.dp,
            ) {
                val contentWidth =
                    itemSize * items.size + itemGap * (items.size - 1) + containerPadding * 2

                Box(
                    modifier = Modifier
                        .width(contentWidth)
                        .height(barHeight)
                        .padding(horizontal = containerPadding),
                ) {
                    // Sliding pill indicator — primaryContainer (MakoSU), not secondaryContainer
                    val indicatorOffset = itemSize * selectedPosition + itemGap * selectedPosition
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = indicatorOffset)
                            .size(itemSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )

                    Row(
                        modifier = Modifier.matchParentSize(),
                        horizontalArrangement = Arrangement.spacedBy(itemGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEachIndexed { index, item ->
                            val selected = mainPagerState.selectedPage == index
                            val iconScale by animateFloatAsState(
                                targetValue = if (selected) 1.08f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                                label = "nav_icon_scale_$index",
                            )
                            val interaction = remember(index) { MutableInteractionSource() }

                            Box(
                                modifier = Modifier
                                    .size(itemSize)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = interaction,
                                        indication = ripple(
                                            bounded = true,
                                            radius = itemSize / 2,
                                        ),
                                        role = Role.Tab,
                                        onClick = {
                                            onUserInteraction?.invoke()
                                            if (!selected) mainPagerState.animateToPage(index)
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelRes),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(iconScale),
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MakoStandardBar(
    modifier: Modifier = Modifier,
    onUserInteraction: (() -> Unit)? = null,
) {
    val mainPagerState = LocalMainPagerState.current
    val hasCustomBackground = LocalCustomBackgroundEnabled.current
    val items = mainNavItems
    val containerColor = if (hasCustomBackground) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    } else {
        NavigationBarDefaults.containerColor
    }

    NavigationBar(
        modifier = modifier,
        tonalElevation = if (hasCustomBackground) 0.dp else 6.dp,
        containerColor = containerColor,
    ) {
        items.forEachIndexed { index, item ->
            val selected = mainPagerState.selectedPage == index
            NavigationBarItem(
                selected = selected,
                onClick = {
                    onUserInteraction?.invoke()
                    if (!selected) mainPagerState.animateToPage(index)
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp,
                    )
                },
                // MakoSU fixed bar: show labels (clearer than icon-only FolkPatch floating)
                alwaysShowLabel = true,
            )
        }
    }
}
