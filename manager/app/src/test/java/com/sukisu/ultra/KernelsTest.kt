package com.sukisu.ultra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KernelsTest {
    @Test
    fun `recognizes supported GKI 2 kernel versions`() {
        assertFalse(KernelVersion(5, 4, 302).isGKI())
        assertTrue(KernelVersion(5, 10, 218).isGKI())
        assertTrue(KernelVersion(6, 6, 1).isGKI())
        assertFalse(KernelVersion(4, 19, 325).isGKI())
    }

    @Test
    fun `uses init boot only from Android 13`() {
        assertEquals("boot", defaultBootPartitionForSdk(30))
        assertEquals("boot", defaultBootPartitionForSdk(32))
        assertEquals("init_boot", defaultBootPartitionForSdk(33))
    }
}
