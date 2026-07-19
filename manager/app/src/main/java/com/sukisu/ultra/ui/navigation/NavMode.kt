package com.sukisu.ultra.ui.navigation

import androidx.annotation.StringRes
import com.sukisu.ultra.R

/**
 * FolkPatch `nav_mode` equivalents.
 */
enum class NavMode(
    val value: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
) {
    Floating(
        value = "floating",
        titleRes = R.string.settings_nav_mode_floating,
        summaryRes = R.string.settings_nav_mode_floating_summary,
    ),
    Bottom(
        value = "bottom",
        titleRes = R.string.settings_nav_mode_bottom,
        summaryRes = R.string.settings_nav_mode_bottom_summary,
    ),
    Rail(
        value = "rail",
        titleRes = R.string.settings_nav_mode_rail,
        summaryRes = R.string.settings_nav_mode_rail_summary,
    ),
    Auto(
        value = "auto",
        titleRes = R.string.settings_nav_mode_auto,
        summaryRes = R.string.settings_nav_mode_auto_summary,
    );

    companion object {
        const val DEFAULT_VALUE = "floating"

        fun fromValue(value: String?): NavMode = when (value) {
            Bottom.value -> Bottom
            Rail.value -> Rail
            Auto.value -> Auto
            Floating.value -> Floating
            else -> Floating
        }
    }
}
