package com.sukisu.ultra.ui.screen.susfs.util

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.util.getSuSFSStatus
import com.sukisu.ultra.ui.util.spoofKernelUname
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SuSFSRepository {
    suspend fun getCurrentModuleConfig(): ModuleConfig {
        val config = SuSFSConfig.dump()
        fun value(key: String): String = config[key].orEmpty()
        fun enabled(key: String): Boolean = value(key) == "true"
        fun values(key: String, separator: String = ";"): Set<String> =
            value(key).split(separator).filterTo(linkedSetOf()) { it.isNotBlank() }

        return ModuleConfig(
            unameValue = value(SuSFSConfig.KEY_UNAME_VALUE).ifBlank { SuSFSConfig.DEFAULT_UNAME },
            buildTimeValue = value(SuSFSConfig.KEY_BUILD_TIME_VALUE).ifBlank { SuSFSConfig.DEFAULT_BUILD_TIME },
            autoStartEnabled = enabled(SuSFSConfig.KEY_AUTO_START_ENABLED),
            executeInPostFsData = enabled(SuSFSConfig.KEY_EXECUTE_IN_POST_FS_DATA),
            susPaths = values(SuSFSConfig.KEY_SUS_PATHS),
            susLoopPaths = values(SuSFSConfig.KEY_SUS_LOOP_PATHS),
            susMaps = values(SuSFSConfig.KEY_SUS_MAPS),
            enableLog = enabled(SuSFSConfig.KEY_ENABLE_LOG),
            kstatConfigs = values(SuSFSConfig.KEY_KSTAT_CONFIGS, ";;"),
            addKstatPaths = values(SuSFSConfig.KEY_ADD_KSTAT_PATHS),
            hideSusMountsForAllProcs = enabled(SuSFSConfig.KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS),
            enableHideBl = enabled(SuSFSConfig.KEY_ENABLE_HIDE_BL),
            enableCleanupResidue = enabled(SuSFSConfig.KEY_ENABLE_CLEANUP_RESIDUE),
            enableAvcLogSpoofing = enabled(SuSFSConfig.KEY_ENABLE_AVC_LOG_SPOOFING)
        )
    }

    private suspend fun getUnameValue(): String {
        val v = SuSFSConfig.get(SuSFSConfig.KEY_UNAME_VALUE)
        return v.ifBlank { SuSFSConfig.DEFAULT_UNAME }
    }

    private suspend fun getBuildTimeValue(): String {
        val v = SuSFSConfig.get(SuSFSConfig.KEY_BUILD_TIME_VALUE)
        return v.ifBlank { SuSFSConfig.DEFAULT_BUILD_TIME }
    }

    suspend fun getKernelSpoofRelease(): String =
        getUnameValue().takeUnless { SuSFSConfig.isDefaultSpoofValue(it) }.orEmpty()

    suspend fun getKernelSpoofVersion(): String =
        getBuildTimeValue().takeUnless { SuSFSConfig.isDefaultSpoofValue(it) }.orEmpty()

    suspend fun setAutoStartEnabled(enabled: Boolean) =
        SuSFSConfig.set(SuSFSConfig.KEY_AUTO_START_ENABLED, if (enabled) "true" else "false")

    suspend fun isAutoStartEnabled(): Boolean =
        SuSFSConfig.get(SuSFSConfig.KEY_AUTO_START_ENABLED) == "true"

    suspend fun getEnableLogState(): Boolean =
        SuSFSConfig.get(SuSFSConfig.KEY_ENABLE_LOG) == "true"

    suspend fun getExecuteInPostFsData(): Boolean =
        SuSFSConfig.get(SuSFSConfig.KEY_EXECUTE_IN_POST_FS_DATA) == "true"

    suspend fun getHideSusMountsForAllProcs(): Boolean =
        SuSFSConfig.get(SuSFSConfig.KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS) == "true"

    suspend fun getEnableHideBl(): Boolean =
        SuSFSConfig.get(SuSFSConfig.KEY_ENABLE_HIDE_BL)  == "true"

    suspend fun getEnableCleanupResidue(): Boolean =
        SuSFSConfig.get(SuSFSConfig.KEY_ENABLE_CLEANUP_RESIDUE) == "true"

    suspend fun getEnableAvcLogSpoofing(): Boolean =
        SuSFSConfig.get(SuSFSConfig.KEY_ENABLE_AVC_LOG_SPOOFING) == "true"

    suspend fun getSusPaths(): Set<String> =
        SuSFSConfig.getMulti(SuSFSConfig.KEY_SUS_PATHS)

    suspend fun getSusLoopPaths(): Set<String> =
        SuSFSConfig.getMulti(SuSFSConfig.KEY_SUS_LOOP_PATHS)

    suspend fun getSusMaps(): Set<String> =
        SuSFSConfig.getMulti(SuSFSConfig.KEY_SUS_MAPS)

    suspend fun getKstatConfigs(): Set<String> =
        SuSFSConfig.getMulti(SuSFSConfig.KEY_KSTAT_CONFIGS, ";;")

    suspend fun getAddKstatPaths(): Set<String> =
        SuSFSConfig.getMulti(SuSFSConfig.KEY_ADD_KSTAT_PATHS)

    suspend fun saveUnameValue(value: String) {
        SuSFSConfig.set(SuSFSConfig.KEY_UNAME_VALUE, value)
    }

    suspend fun saveBuildTimeValue(value: String) {
        SuSFSConfig.set(SuSFSConfig.KEY_BUILD_TIME_VALUE, value)
    }

    suspend fun saveEnableLogState(enabled: Boolean) {
        SuSFSConfig.set(SuSFSConfig.KEY_ENABLE_LOG, if (enabled) "true" else "false")
    }

    suspend fun saveExecuteInPostFsData(enabled: Boolean) {
        SuSFSConfig.set(SuSFSConfig.KEY_EXECUTE_IN_POST_FS_DATA, if (enabled) "true" else "false")
    }

    suspend fun saveHideSusMountsForAllProcs(hideForAll: Boolean) {
        SuSFSConfig.set(SuSFSConfig.KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS, if (hideForAll) "true" else "false")
    }

    suspend fun saveEnableHideBl(enabled: Boolean) {
        SuSFSConfig.set(SuSFSConfig.KEY_ENABLE_HIDE_BL, if (enabled) "true" else "false")
    }

    suspend fun saveEnableCleanupResidue(enabled: Boolean) {
        SuSFSConfig.set(SuSFSConfig.KEY_ENABLE_CLEANUP_RESIDUE, if (enabled) "true" else "false")
    }

    suspend fun saveEnableAvcLogSpoofing(enabled: Boolean) {
        SuSFSConfig.set(SuSFSConfig.KEY_ENABLE_AVC_LOG_SPOOFING, if (enabled) "true" else "false")
    }

    suspend fun saveSusPaths(paths: Set<String>) {
        SuSFSConfig.setMulti(SuSFSConfig.KEY_SUS_PATHS, paths, ";")
    }

    suspend fun saveSusLoopPaths(paths: Set<String>) {
        SuSFSConfig.setMulti(SuSFSConfig.KEY_SUS_LOOP_PATHS, paths, ";")
    }

    suspend fun saveSusMaps(maps: Set<String>) {
        SuSFSConfig.setMulti(SuSFSConfig.KEY_SUS_MAPS, maps, ";")
    }

    suspend fun saveKstatConfigs(configs: Set<String>) {
        SuSFSConfig.setMulti(SuSFSConfig.KEY_KSTAT_CONFIGS, configs, ";;")
    }

    suspend fun saveAddKstatPaths(paths: Set<String>) {
        SuSFSConfig.setMulti(SuSFSConfig.KEY_ADD_KSTAT_PATHS, paths, ";")
    }

    suspend fun setEnableLog(context: Context, enabled: Boolean): Boolean {
        val success = SuSFSCommands.executeSusfsCommand(context, "enable-log ${if (enabled) 1 else 0}")
        if (success) {
            saveEnableLogState(enabled)
            if (isAutoStartEnabled()) SuSFSCommands.updateMagiskModule()
        }
        return success
    }

    suspend fun setEnableAvcLogSpoofing(context: Context, enabled: Boolean): Boolean {
        val success = SuSFSCommands.executeSusfsCommand(context, "enable-avc-log-spoofing ${if (enabled) 1 else 0}")
        if (success) {
            saveEnableAvcLogSpoofing(enabled)
            if (isAutoStartEnabled()) SuSFSCommands.updateMagiskModule()
        }
        return success
    }

    suspend fun setHideSusMountsForAllProcs(context: Context, hideForAll: Boolean): Boolean {
        val success = SuSFSCommands.executeSusfsCommand(context, "hide-sus-mnts-for-non-su-procs ${if (hideForAll) 1 else 0}")
        if (success) {
            saveHideSusMountsForAllProcs(hideForAll)
            if (isAutoStartEnabled()) SuSFSCommands.updateMagiskModule()
        }
        return success
    }

    @SuppressLint("StringFormatMatches")
    suspend fun setUname(context: Context, unameValue: String, buildTimeValue: String): Boolean {
        val useSusfs = try {
            getSuSFSStatus().equals("true", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
        val success = if (useSusfs) {
            val susfsResult = SuSFSCommands.executeSusfsCommandWithOutput(
                "set-uname ${SuSFSConfig.shellQuote(unameValue)} ${SuSFSConfig.shellQuote(buildTimeValue)}"
            )
            susfsResult.isSuccess || spoofKernelUname(unameValue, buildTimeValue)
        } else {
            spoofKernelUname(unameValue, buildTimeValue)
        }
        if (!success) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.susfs_command_failed), Toast.LENGTH_SHORT).show()
            }
        }
        if (success) {
            saveUnameValue(unameValue)
            saveBuildTimeValue(buildTimeValue)
            if (isAutoStartEnabled()) SuSFSCommands.updateMagiskModule()
        }
        return success
    }
}
