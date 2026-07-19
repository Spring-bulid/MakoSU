package com.sukisu.ultra.ui.screen.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutTest {
    @Test
    fun parsesAllPersistedLayouts() {
        HomeLayout.entries.forEach { layout ->
            assertEquals(layout, HomeLayout.fromValue(layout.value))
        }
    }

    @Test
    fun migratesLegacyLayoutsToCircle() {
        assertEquals(HomeLayout.Circle, HomeLayout.fromValue("compact"))
        assertEquals(HomeLayout.Circle, HomeLayout.fromValue("legacy_layout"))
    }

    @Test
    fun fallsBackToCircleForUnknownLayout() {
        assertEquals(HomeLayout.Circle, HomeLayout.fromValue("unknown"))
    }
}
