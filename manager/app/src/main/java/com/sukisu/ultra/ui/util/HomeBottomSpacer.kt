package com.sukisu.ultra.ui.util

/**
 * Cloned from FolkPatch:
 * me.bmax.apatch.util.ui.HomeBottomSpacer
 *
 * Exact behavior:
 * - floating + bar visible → 80.dp + system navigation bar
 * - floating + bar hidden  → system navigation bar only
 * - else (fixed bottom / rail outer pad) → 16.dp
 */

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sukisu.ultra.ui.navigation.LocalBottomBarVisible
import com.sukisu.ultra.ui.navigation.LocalIsFloatingNavMode

@Composable
fun HomeBottomSpacer(modifier: Modifier = Modifier) {
    val isFloatingMode = LocalIsFloatingNavMode.current
    val bottomBarVisible = LocalBottomBarVisible.current.value
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val targetHeight by animateDpAsState(
        targetValue = when {
            isFloatingMode && bottomBarVisible -> 80.dp + navBarBottom
            isFloatingMode -> navBarBottom
            else -> 16.dp
        },
        animationSpec = tween(durationMillis = 300),
        label = "homeBottomSpacer",
    )

    Spacer(modifier.height(targetHeight))
}
