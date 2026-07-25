package com.sukisu.ultra.ui.component.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * FolkPatch-style main page state: tab switches are instant jumps;
 * the fade in/out is applied by the page container (AnimatedVisibility).
 */
class MainPagerState(
    initialPage: Int,
) {
    var selectedPage by mutableIntStateOf(initialPage)
        private set

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return
        selectedPage = targetIndex
    }
}

@Composable
fun rememberMainPagerState(
    initialPage: Int,
): MainPagerState {
    return remember(initialPage) {
        MainPagerState(initialPage)
    }
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    isFloating: Boolean = true,
    onUserInteraction: (() -> Unit)? = null,
) {
    BottomBarMaterial(
        modifier = modifier,
        isFloating = isFloating,
        onUserInteraction = onUserInteraction,
    )
}

@Composable
fun SideRail(
    modifier: Modifier = Modifier,
) {
    NavigationRailMaterial(modifier)
}
