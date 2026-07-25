package com.sukisu.ultra.ui.theme

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.io.File

/**
 * Per-page backgrounds (FolkPatch home/superuser/module/settings background URIs).
 * Each main tab can have its own background image; falls back to the global
 * custom background when unset. Images live in internal storage.
 */
object PageBackgrounds {
    private const val PREFS_NAME = "settings"
    private const val KEY_PREFIX = "page_bg_"
    private const val FILE_PREFIX = "page_bg_"
    private const val FILE_SUFFIX = ".jpg"

    const val PAGE_HOME = 0
    const val PAGE_SUPERUSER = 1
    const val PAGE_MODULE = 2
    const val PAGE_SETTINGS = 3
    val PAGES = listOf(PAGE_HOME, PAGE_SUPERUSER, PAGE_MODULE, PAGE_SETTINGS)

    var revision by mutableIntStateOf(0)
        private set

    /** page -> image set? (snapshot state for recomposition) */
    val setStates = mutableStateMapOf<Int, Boolean>()

    fun file(context: Context, page: Int): File =
        File(context.filesDir, "$FILE_PREFIX$page$FILE_SUFFIX")

    fun isSet(page: Int): Boolean = setStates[page] == true

    fun uriFor(context: Context, page: Int): String? =
        if (isSet(page) && file(context, page).exists()) {
            Uri.fromFile(file(context, page)).toString()
        } else {
            null
        }

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        PAGES.forEach { page ->
            setStates[page] = prefs.getBoolean("$KEY_PREFIX$page", false) &&
                file(context, page).exists()
        }
    }

    fun saveImage(context: Context, page: Int, uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file(context, page).outputStream().use { output -> input.copyTo(output) }
            } ?: return false
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean("$KEY_PREFIX$page", true).apply()
            setStates[page] = true
            revision++
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreImage(context: Context, page: Int, bytes: ByteArray) {
        file(context, page).writeBytes(bytes)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("$KEY_PREFIX$page", true).apply()
        setStates[page] = true
        revision++
    }

    fun imageBytes(context: Context, page: Int): ByteArray? =
        if (isSet(page) && file(context, page).exists()) file(context, page).readBytes() else null

    fun clear(context: Context, page: Int) {
        file(context, page).delete()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("$KEY_PREFIX$page", false).apply()
        setStates[page] = false
        revision++
    }
}
