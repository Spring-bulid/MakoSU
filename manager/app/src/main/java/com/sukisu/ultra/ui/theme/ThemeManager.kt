package com.sukisu.ultra.ui.theme

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import com.sukisu.ultra.ui.screen.home.GridWorkingCardBg
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Theme import/export (FolkPatch ThemeManager equivalent, MakoSU scope).
 * A .mkt file is a zip: theme.json + optional background.jpg / font.ttf / grid_card.jpg.
 */
object ThemeManager {
    private const val PREFS_NAME = "settings"
    private const val FORMAT = "makosu-theme"
    private const val ENTRY_JSON = "theme.json"
    private const val ENTRY_BACKGROUND = "background.jpg"
    private const val ENTRY_FONT = "font.ttf"
    private const val ENTRY_GRID_CARD = "grid_card.jpg"
    private const val ENTRY_PAGE_BG = "page_bg_"
    private const val IMPORTED_BACKGROUND_NAME = "theme_background_imported.jpg"

    private val boolKeys = listOf(
        "custom_background_enabled",
        "module_banner_enabled",
        "module_banner_custom_enabled",
        "module_banner_custom_opacity_enabled",
        "floating_auto_hide",
        "floating_swipe_hide",
        "show_fingerprint",
    )
    private val intKeys = listOf("color_mode", "key_color")
    private val floatKeys = listOf(
        "custom_background_opacity",
        "custom_background_blur",
        "custom_background_dim",
        "module_banner_opacity",
        "page_scale",
        "grid_working_card_opacity",
        "grid_working_card_dim",
        "grid_working_card_day_opacity",
        "grid_working_card_night_opacity",
    )
    private val stringKeys = listOf("color_style", "color_spec", "home_layout", "nav_mode")

    fun exportTheme(context: Context, target: Uri): Boolean = try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONObject()
        json.put("format", FORMAT)
        json.put("version", 1)
        boolKeys.forEach { json.put(it, prefs.getBoolean(it, false)) }
        intKeys.forEach { json.put(it, prefs.getInt(it, 0)) }
        floatKeys.forEach { json.put(it, prefs.getFloat(it, 0f).toDouble()) }
        stringKeys.forEach { key -> prefs.getString(key, null)?.let { json.put(key, it) } }
        json.put("grid_working_card_bg", GridWorkingCardBg.isSet)
        val fontEnabled = FontConfig.isCustomFontEnabled && FontConfig.customFontFilename != null
        json.put("custom_font_enabled", fontEnabled)

        context.contentResolver.openOutputStream(target)?.use { os ->
            ZipOutputStream(os.buffered()).use { zip ->
                zip.putNextEntry(ZipEntry(ENTRY_JSON))
                zip.write(json.toString().toByteArray())
                zip.closeEntry()

                val bgUri = prefs.getString("custom_background_uri", null)
                if (prefs.getBoolean("custom_background_enabled", false) && !bgUri.isNullOrBlank()) {
                    runCatching {
                        context.contentResolver.openInputStream(Uri.parse(bgUri))?.use { input ->
                            zip.putNextEntry(ZipEntry(ENTRY_BACKGROUND))
                            input.copyTo(zip)
                            zip.closeEntry()
                        }
                    }
                }

                if (fontEnabled) {
                    val fontFile = File(context.filesDir, FontConfig.customFontFilename!!)
                    if (fontFile.exists()) {
                        zip.putNextEntry(ZipEntry(ENTRY_FONT))
                        fontFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }

                GridWorkingCardBg.imageBytes(context)?.let { bytes ->
                    zip.putNextEntry(ZipEntry(ENTRY_GRID_CARD))
                    zip.write(bytes)
                    zip.closeEntry()
                }

                PageBackgrounds.PAGES.forEach { page ->
                    PageBackgrounds.imageBytes(context, page)?.let { bytes ->
                        zip.putNextEntry(ZipEntry("$ENTRY_PAGE_BG$page.jpg"))
                        zip.write(bytes)
                        zip.closeEntry()
                    }
                }
            }
        } ?: return false
        true
    } catch (e: Exception) {
        false
    }

    fun importTheme(context: Context, source: Uri): Boolean = try {
        var json: JSONObject? = null
        var background: ByteArray? = null
        var font: ByteArray? = null
        var gridCard: ByteArray? = null
        val pageBgs = mutableMapOf<Int, ByteArray>()

        context.contentResolver.openInputStream(source)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when {
                        entry.name == ENTRY_JSON -> json = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                        entry.name == ENTRY_BACKGROUND -> background = zip.readBytes()
                        entry.name == ENTRY_FONT -> font = zip.readBytes()
                        entry.name == ENTRY_GRID_CARD -> gridCard = zip.readBytes()
                        entry.name.startsWith(ENTRY_PAGE_BG) -> {
                            entry.name.removePrefix(ENTRY_PAGE_BG).removeSuffix(".jpg").toIntOrNull()
                                ?.let { page -> pageBgs[page] = zip.readBytes() }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: return false

        val theme = json ?: return false
        if (theme.optString("format") != FORMAT) return false

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            boolKeys.forEach { if (theme.has(it)) putBoolean(it, theme.getBoolean(it)) }
            intKeys.forEach { if (theme.has(it)) putInt(it, theme.getInt(it)) }
            floatKeys.forEach { if (theme.has(it)) putFloat(it, theme.getDouble(it).toFloat()) }
            stringKeys.forEach { if (theme.has(it)) putString(it, theme.getString(it)) }

            if (background != null) {
                val file = File(context.filesDir, IMPORTED_BACKGROUND_NAME)
                file.writeBytes(background)
                putString("custom_background_uri", Uri.fromFile(file).toString())
                putBoolean("custom_background_enabled", theme.optBoolean("custom_background_enabled", true))
            } else if (theme.has("custom_background_enabled") && !theme.getBoolean("custom_background_enabled")) {
                putBoolean("custom_background_enabled", false)
            }

            if (gridCard != null) {
                GridWorkingCardBg.restoreImage(context, gridCard)
            } else {
                putBoolean("grid_working_card_bg", theme.optBoolean("grid_working_card_bg", false))
            }
        }

        pageBgs.forEach { (page, bytes) -> PageBackgrounds.restoreImage(context, page, bytes) }

        if (font != null) {
            val tmp = File(context.cacheDir, "theme_font_import.ttf")
            tmp.writeBytes(font)
            FontConfig.applyCustomFont(context, tmp)
            tmp.delete()
            if (!theme.optBoolean("custom_font_enabled", true)) {
                FontConfig.setCustomFontEnabledState(false)
                FontConfig.save(context)
            }
        } else if (theme.has("custom_font_enabled") && !theme.getBoolean("custom_font_enabled")) {
            FontConfig.clearFont(context)
            FontConfig.setCustomFontEnabledState(false)
            FontConfig.save(context)
        }
        FontConfig.invalidateFontCache()
        true
    } catch (e: Exception) {
        false
    }
}
