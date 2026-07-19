package com.sukisu.ultra.ui.screen.home

import androidx.annotation.StringRes
import com.sukisu.ultra.R

/**
 * Home layouts: StatsUI + PureUI only.
 * Persist via settings key `home_layout`.
 */
enum class HomeLayout(
    val value: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val summaryRes: Int,
) {
    Stats(
        value = "stats",
        titleRes = R.string.settings_home_layout_stats,
        summaryRes = R.string.settings_home_layout_stats_summary,
    ),
    Pure(
        value = "pure",
        titleRes = R.string.settings_home_layout_pure,
        summaryRes = R.string.settings_home_layout_pure_summary,
    );

    companion object {
        const val DEFAULT_VALUE = "pure"

        fun fromValue(value: String?): HomeLayout = when (value?.trim()?.lowercase()) {
            "stats" -> Stats
            "pure", "minimal", "simple", "clean" -> Pure
            // Former layouts → PureUI
            else -> Pure
        }
    }
}
