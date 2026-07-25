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
    fun migratesLegacyLayoutsToPure() {
        assertEquals(HomeLayout.Pure, HomeLayout.fromValue("compact"))
        assertEquals(HomeLayout.Pure, HomeLayout.fromValue("legacy_layout"))
    }

    @Test
    fun fallsBackToPureForUnknownLayout() {
        assertEquals(HomeLayout.Pure, HomeLayout.fromValue("unknown"))
    }
}
