package com.sukisu.ultra.ui.screen.home

import androidx.annotation.StringRes
import com.sukisu.ultra.R

/**
 * Home layouts: StatsUI / PureUI / GridUI / ListUI.
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
    ),
    Grid(
        value = "grid",
        titleRes = R.string.settings_home_layout_grid,
        summaryRes = R.string.settings_home_layout_grid_summary,
    ),
    List(
        value = "list",
        titleRes = R.string.settings_home_layout_list,
        summaryRes = R.string.settings_home_layout_list_summary,
    );

    companion object {
        const val DEFAULT_VALUE = "pure"

        fun fromValue(value: String?): HomeLayout = when (value?.trim()?.lowercase()) {
            "stats" -> Stats
            "pure", "minimal", "simple", "clean" -> Pure
            "grid", "kernelsu" -> Grid
            "list" -> List
            // Former layouts → PureUI
            else -> Pure
        }
    }
}
