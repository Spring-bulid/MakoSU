package com.sukisu.ultra.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Global UI refresh token.
 * Appearance writes prefs then calls [bump]; Home/nav consumers re-read prefs.
 */
object UiRefresh {
    private val _token = MutableStateFlow(0L)
    val token: StateFlow<Long> = _token.asStateFlow()

    /** Bump revision so collectors re-sync (must not be named notify — clashes with Object.notify). */
    fun bump() {
        _token.update { it + 1L }
    }
}
