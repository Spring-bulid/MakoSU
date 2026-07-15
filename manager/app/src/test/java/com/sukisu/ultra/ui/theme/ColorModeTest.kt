package com.sukisu.ultra.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorModeTest {
    @Test
    fun `amoled is not classified as monet`() {
        assertFalse(ColorMode.DARK_AMOLED.isMonet)
        assertTrue(ColorMode.MONET_DARK.isMonet)
    }

    @Test
    fun `miuix converts amoled to a supported dark mode`() {
        assertEquals(ColorMode.DARK, ColorMode.DARK_AMOLED.forMiuix(monetEnabled = false))
        assertEquals(ColorMode.MONET_DARK, ColorMode.DARK_AMOLED.forMiuix(monetEnabled = true))
    }

    @Test
    fun `miuix monet toggle preserves light and dark intent`() {
        assertEquals(ColorMode.MONET_LIGHT, ColorMode.LIGHT.forMiuix(monetEnabled = true))
        assertEquals(ColorMode.DARK, ColorMode.MONET_DARK.forMiuix(monetEnabled = false))
    }
}
