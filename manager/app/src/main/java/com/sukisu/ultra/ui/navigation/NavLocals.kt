package com.sukisu.ultra.ui.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

/** FolkPatch-style scroll tracking for floating bottom bar hide/show. */
data class NavScrollState(
    val isScrollingDown: MutableState<Boolean>,
    val scrollOffset: MutableState<Float>,
    val previousScrollOffset: MutableState<Float>,
)

val LocalNavScrollState = compositionLocalOf<NavScrollState?> { null }
val LocalBottomBarVisible = compositionLocalOf { mutableStateOf(true) }
val LocalIsFloatingNavMode = compositionLocalOf { false }

@Composable
fun rememberNavScrollConnection(
    isScrollingDown: MutableState<Boolean>,
    scrollOffset: MutableState<Float>,
    previousScrollOffset: MutableState<Float>,
    threshold: Float = 50f,
    onUserScroll: (() -> Unit)? = null,
): NestedScrollConnection {
    return remember(onUserScroll, threshold) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta != 0f) {
                    onUserScroll?.invoke()
                }
                val newOffset = scrollOffset.value + delta
                scrollOffset.value = newOffset
                val scrollDelta = previousScrollOffset.value - newOffset
                if (abs(scrollDelta) > threshold) {
                    isScrollingDown.value = scrollDelta > 0
                    previousScrollOffset.value = newOffset
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                previousScrollOffset.value = scrollOffset.value
                return super.onPostFling(consumed, available)
            }
        }
    }
}
