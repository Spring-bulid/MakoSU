package com.sukisu.ultra.ui.theme

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * In-app language override (FolkPatch LanguagePicker equivalent).
 * No appcompat: locale is applied via attachBaseContext + createConfigurationContext,
 * persisted in SharedPreferences. null/blank tag = follow system.
 */
object AppLanguage {
    private const val PREFS_NAME = "settings"
    private const val KEY_APP_LANGUAGE = "app_language"

    /** All locales shipped in res/values-*. BCP-47 tags (values-zh-rCN -> zh-CN). */
    val SUPPORTED_LANGUAGES: List<String> = listOf(
        "en",
        "ar", "az", "bg", "bn", "bn-BD", "bs", "da", "de", "es", "et", "fa",
        "fil", "fr", "gl", "hi", "hr", "hu", "in", "it", "iw", "ja", "km",
        "kn", "ko", "lt", "lv", "mr", "ms", "my", "nl", "pl", "pt", "pt-BR",
        "ro", "ru", "sl", "sr", "te", "th", "tl", "tr", "uk", "vi",
        "zh-CN", "zh-HK", "zh-TW",
    )

    fun currentLanguageTag(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_LANGUAGE, null)
            ?.takeIf { it.isNotBlank() }

    fun setLanguage(context: Context, tag: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_LANGUAGE, tag ?: "")
            .apply()
    }

    fun nativeName(tag: String): String {
        val locale = Locale.forLanguageTag(tag)
        return locale.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
    }

    fun wrap(context: Context): Context {
        val tag = currentLanguageTag(context) ?: return context
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
