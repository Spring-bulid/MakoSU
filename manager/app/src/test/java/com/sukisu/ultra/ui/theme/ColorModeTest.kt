package com.sukisu.ultra.ui.theme

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorModeTest {
    @Test
    fun `amoled is not classified as monet`() {
        assertFalse(ColorMode.DARK_AMOLED.isMonet)
        assertTrue(ColorMode.MONET_DARK.isMonet)
    }
}
