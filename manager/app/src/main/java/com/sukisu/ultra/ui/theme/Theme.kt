package com.sukisu.ultra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.sukisu.ultra.data.repository.SettingsRepository
import com.sukisu.ultra.data.repository.SettingsRepositoryImpl

enum class ColorMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2),
    MONET_SYSTEM(3),
    MONET_LIGHT(4),
    MONET_DARK(5),
    DARK_AMOLED(6);

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: SYSTEM
    }

    val isSystem: Boolean get() = value == 0 || value == 3
    val isDark: Boolean get() = value == 2 || value == 5 || value == 6
    val isAmoled: Boolean get() = value == 6
    val isMonet: Boolean get() = this == MONET_SYSTEM || this == MONET_LIGHT || this == MONET_DARK

}

data class AppSettings(
    val colorMode: ColorMode,
    val keyColor: Int,
    val paletteStyle: PaletteStyle,
    val colorSpec: ColorSpec.SpecVersion,
)

val PaletteStyle.supportsSpec2025: Boolean
    get() = this == PaletteStyle.TonalSpot ||
            this == PaletteStyle.Neutral ||
            this == PaletteStyle.Vibrant ||
            this == PaletteStyle.Expressive

fun ColorSpec.SpecVersion.effectiveFor(style: PaletteStyle): ColorSpec.SpecVersion =
    if (this == ColorSpec.SpecVersion.SPEC_2025 && !style.supportsSpec2025) {
        ColorSpec.SpecVersion.SPEC_2021
    } else {
        this
    }

object ThemeController {
    fun getAppSettings(repo: SettingsRepository = SettingsRepositoryImpl()): AppSettings {
        val colorMode = ColorMode.fromValue(repo.themeMode)
        val keyColor = repo.keyColor
        val paletteStyleStr = repo.colorStyle
        val paletteStyle = try {
            PaletteStyle.valueOf(paletteStyleStr)
        } catch (_: Exception) {
            PaletteStyle.TonalSpot
        }
        val colorSpecStr = repo.colorSpec
        val colorSpec = try {
            ColorSpec.SpecVersion.valueOf(colorSpecStr)
        } catch (_: Exception) {
            ColorSpec.SpecVersion.SPEC_2025
        }

        return AppSettings(colorMode, keyColor, paletteStyle, colorSpec)
    }
}

@Composable
fun KernelSUTheme(
    appSettings: AppSettings = ThemeController.getAppSettings(),
    content: @Composable () -> Unit
) {
    MaterialKernelSUTheme(
        appSettings = appSettings,
        content = content
    )
}

@Composable
@ReadOnlyComposable
fun isInDarkTheme(): Boolean {
    return when (LocalColorMode.current) {
        1, 4 -> false  // Force light mode
        2, 5, 6 -> true   // Force dark mode
        else -> isSystemInDarkTheme()  // Follow system (0 or default)
    }
}


val LocalColorMode = staticCompositionLocalOf { 0 }

val LocalCustomBackgroundEnabled = staticCompositionLocalOf { false }

val LocalCustomBackgroundOpacity = staticCompositionLocalOf { 1f }
