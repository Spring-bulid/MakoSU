package com.sukisu.ultra.ui.component

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class FastBoxBlurTest {
    @Test
    fun zeroRadiusDoesNotChangePixels() {
        val pixels = intArrayOf(0xff000000.toInt(), 0xffffffff.toInt())
        val expected = pixels.copyOf()

        FastBoxBlur.apply(pixels, width = 2, height = 1, radius = 0)

        assertArrayEquals(expected, pixels)
    }

    @Test
    fun uniformColorIsPreserved() {
        val color = 0xff336699.toInt()
        val pixels = IntArray(12) { color }

        FastBoxBlur.apply(pixels, width = 4, height = 3, radius = 2, passes = 3)

        assertArrayEquals(IntArray(12) { color }, pixels)
    }

    @Test
    fun impulseIsDistributedAcrossNeighbors() {
        val pixels = intArrayOf(
            0xff000000.toInt(),
            0xffffffff.toInt(),
            0xff000000.toInt(),
        )

        FastBoxBlur.apply(pixels, width = 3, height = 1, radius = 1, passes = 1)

        assertArrayEquals(IntArray(3) { 0xff555555.toInt() }, pixels)
    }
}
