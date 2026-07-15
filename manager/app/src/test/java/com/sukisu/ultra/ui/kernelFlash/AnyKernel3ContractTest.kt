package com.sukisu.ultra.ui.kernelFlash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import com.sukisu.ultra.ui.kernelFlash.state.HorizonKernelState

class AnyKernel3ContractTest {
    @Test
    fun `accepts a standard AnyKernel3 archive at its root`() {
        assertTrue(
            AnyKernel3Contract.isSupportedArchive(
                listOf(
                    "anykernel.sh",
                    "tools/ak3-core.sh",
                    AnyKernel3Contract.UPDATE_BINARY_ENTRY
                )
            )
        )
    }

    @Test
    fun `rejects incomplete and wrapped archives`() {
        assertFalse(
            AnyKernel3Contract.isSupportedArchive(
                listOf("anykernel.sh", "tools/ak3-core.sh")
            )
        )
        assertFalse(
            AnyKernel3Contract.isSupportedArchive(
                listOf(
                    "kernel/anykernel.sh",
                    "kernel/tools/ak3-core.sh",
                    "kernel/${AnyKernel3Contract.UPDATE_BINARY_ENTRY}"
                )
            )
        )
    }

    @Test
    fun `inspects archive entries incrementally`() {
        val archive = AnyKernel3Contract.inspectArchive()
        archive.record("anykernel.sh")
        archive.record("tools/ak3-core.sh")
        assertFalse(archive.isSupported)

        archive.record(AnyKernel3Contract.UPDATE_BINARY_ENTRY)
        assertTrue(archive.isSupported)
    }

    @Test
    fun `patches only the setup invocation`() {
        val script = """
            #!/sbin/sh
            setup_bb() {
              true
            }
            setup_bb;
        """.trimIndent()

        val patched = AnyKernel3Contract.patchUpdateBinary(script, "echo makosu\n")

        assertTrue(patched.contains("setup_bb()"))
        assertTrue(patched.contains("echo makosu\nsetup_bb;"))
    }

    @Test
    fun `generates quoted KPM preparation commands`() {
        val script = AnyKernel3Contract.buildPreparationScript(
            tools = AnyKernel3ToolPaths(
                mkbootfs = "/data/user/0/com.makosu.manager/cache/mk bootfs",
                kptools = "/data/user/0/com.makosu.manager/cache/kp'tools",
                kpimg = "/data/user/0/com.makosu.manager/cache/kpimg"
            ),
            kpmMode = AnyKernel3KpmMode.UNDO
        )

        assertTrue(script.contains("kp'\"'\"'tools'"))
        assertTrue(script.contains(" -u -s 123"))
        assertTrue(script.contains("\$AKHOME/tools/mkbootfs"))
    }

    @Test
    fun `maps only the opposite target slot to inactive`() {
        assertNull(AnyKernel3Contract.slotSelectionEnvironment("a", "_a"))
        assertEquals("inactive", AnyKernel3Contract.slotSelectionEnvironment("b", "_a"))
        assertNull(AnyKernel3Contract.slotSelectionEnvironment(null, null))
    }

    @Test
    fun `rejects invalid target slots and unknown active slots`() {
        assertIllegalArgument { AnyKernel3Contract.slotSelectionEnvironment("c", "a") }
        assertIllegalArgument { AnyKernel3Contract.slotSelectionEnvironment("a", "normal") }
    }

    @Test
    fun `builds a fully quoted update binary command`() {
        val command = AnyKernel3Contract.buildFlashCommand(
            postInstallDirectory = "/data/user/0/com.makosu.manager/cache/session path",
            updateBinary = "/data/user/0/com.makosu.manager/cache/update binary",
            archive = "/data/user/0/com.makosu.manager/cache/kernel.zip",
            slotSelection = "inactive"
        )

        assertEquals(
            "POSTINSTALL='/data/user/0/com.makosu.manager/cache/session path' " +
                "SLOT_SELECT=inactive sh '/data/user/0/com.makosu.manager/cache/update binary' 3 1 " +
                "'/data/user/0/com.makosu.manager/cache/kernel.zip'",
            command
        )
    }

    @Test
    fun `selects the compatible mkbootfs asset`() {
        assertEquals("5_10", AnyKernel3Contract.mkbootfsAsset("5.10.218"))
        assertEquals("5_15+", AnyKernel3Contract.mkbootfsAsset("5.15.153"))
        assertEquals("5_15+", AnyKernel3Contract.mkbootfsAsset("6.6.1"))
        assertEquals("5_15+", AnyKernel3Contract.mkbootfsAsset("unknown"))
    }

    @Test
    fun `bounds flash logs and retains the newest lines`() {
        val state = HorizonKernelState()
        state.startFlashing()
        state.addLogs((0..750).map { "line-$it" })

        val logs = state.state.value.logs
        assertEquals(500, logs.size)
        assertEquals("line-251", logs.first())
        assertEquals("line-750", logs.last())
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
        }
    }
}
