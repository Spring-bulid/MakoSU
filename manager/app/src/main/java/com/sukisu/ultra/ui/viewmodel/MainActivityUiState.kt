package com.sukisu.ultra.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.sukisu.ultra.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val customBackgroundEnabled: Boolean,
    val customBackgroundUri: String?,
    val customBackgroundOpacity: Float,
    val customBackgroundBlur: Float,
    val customBackgroundDim: Float,
)
