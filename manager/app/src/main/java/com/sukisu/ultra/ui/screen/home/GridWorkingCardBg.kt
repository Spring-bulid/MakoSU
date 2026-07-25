package com.sukisu.ultra.ui.screen.home

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/**
 * Grid home layout: custom image for the big working status card.
 * Mirrors FolkPatch BackgroundConfig grid-working-card fields (uri/enabled/opacity/dim/day/night).
 */
object GridWorkingCardBg {
    private const val PREFS_NAME = "settings"
    private const val KEY_ENABLED = "grid_working_card_bg"
    private const val KEY_OPACITY = "grid_working_card_opacity"
    private const val KEY_DIM = "grid_working_card_dim"
    private const val KEY_DAY_OPACITY = "grid_working_card_day_opacity"
    private const val KEY_NIGHT_OPACITY = "grid_working_card_night_opacity"
    const val FILENAME = "grid_working_card_bg.jpg"

    var revision by mutableIntStateOf(0)
        private set

    var isSet by mutableStateOf(false)
        private set

    var opacity by mutableFloatStateOf(1.0f)
        private set

    var dim by mutableFloatStateOf(0.3f)
        private set

    var dayOpacity by mutableFloatStateOf(1.0f)
        private set

    var nightOpacity by mutableFloatStateOf(1.0f)
        private set

    fun file(context: Context): File = File(context.filesDir, FILENAME)

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isSet = prefs.getBoolean(KEY_ENABLED, false) && file(context).exists()
        opacity = prefs.getFloat(KEY_OPACITY, 1.0f)
        dim = prefs.getFloat(KEY_DIM, 0.3f)
        dayOpacity = prefs.getFloat(KEY_DAY_OPACITY, 1.0f)
        nightOpacity = prefs.getFloat(KEY_NIGHT_OPACITY, 1.0f)
    }

    /** FolkPatch BackgroundConfig.getEffectiveGridBackgroundOpacity */
    fun getEffectiveOpacity(isDarkTheme: Boolean): Float =
        if (dayOpacity != nightOpacity) {
            if (isDarkTheme) nightOpacity else dayOpacity
        } else {
            opacity
        }

    fun saveImage(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file(context).outputStream().use { output -> input.copyTo(output) }
            } ?: return false
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ENABLED, true).apply()
            isSet = true
            revision++
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Restore from an exported theme package (bytes already extracted). */
    fun restoreImage(context: Context, bytes: ByteArray) {
        file(context).writeBytes(bytes)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, true).apply()
        load(context)
        revision++
    }

    fun imageBytes(context: Context): ByteArray? =
        if (isSet && file(context).exists()) file(context).readBytes() else null

    fun clear(context: Context) {
        file(context).delete()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, false).apply()
        isSet = false
        revision++
    }
}
