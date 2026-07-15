package com.sukisu.ultra.ui.screen.susfs.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SuSFSModelsTest {
    private fun config(
        enableHideBl: Boolean = false,
        susPaths: Set<String> = emptySet()
    ) = ModuleConfig(
        unameValue = SuSFSConfig.DEFAULT_UNAME,
        buildTimeValue = SuSFSConfig.DEFAULT_BUILD_TIME,
        autoStartEnabled = false,
        executeInPostFsData = false,
        susPaths = susPaths,
        susLoopPaths = emptySet(),
        susMaps = emptySet(),
        enableLog = false,
        kstatConfigs = emptySet(),
        addKstatPaths = emptySet(),
        hideSusMountsForAllProcs = false,
        enableHideBl = enableHideBl,
        enableCleanupResidue = false,
        enableAvcLogSpoofing = false
    )

    @Test
    fun emptyDefaultsDoNotRequireAutoStart() {
        assertFalse(config().hasAutoStartConfig())
    }

    @Test
    fun pathConfigurationRequiresAutoStart() {
        assertTrue(config(susPaths = setOf("/data/example")).hasAutoStartConfig())
    }

    @Test
    fun bootloaderSpoofRequiresAutoStart() {
        assertTrue(config(enableHideBl = true).hasAutoStartConfig())
    }
}
