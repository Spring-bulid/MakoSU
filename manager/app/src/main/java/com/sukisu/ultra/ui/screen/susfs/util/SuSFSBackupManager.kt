package com.sukisu.ultra.ui.screen.susfs.util

import android.content.Context
import android.os.Build
import android.widget.Toast
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.util.getSuSFSVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SuSFSBackupManager {
    private const val MAX_BACKUP_BYTES = 8L * 1024 * 1024

    private suspend fun getAllConfigurations(): Map<String, Any> {
        val config = SuSFSRepository.getCurrentModuleConfig()
        return mapOf(
            SuSFSConfig.KEY_UNAME_VALUE to config.unameValue,
            SuSFSConfig.KEY_BUILD_TIME_VALUE to config.buildTimeValue,
            SuSFSConfig.KEY_AUTO_START_ENABLED to config.autoStartEnabled,
            SuSFSConfig.KEY_SUS_PATHS to config.susPaths,
            SuSFSConfig.KEY_SUS_LOOP_PATHS to config.susLoopPaths,
            SuSFSConfig.KEY_SUS_MAPS to config.susMaps,
            SuSFSConfig.KEY_ENABLE_LOG to config.enableLog,
            SuSFSConfig.KEY_EXECUTE_IN_POST_FS_DATA to config.executeInPostFsData,
            SuSFSConfig.KEY_KSTAT_CONFIGS to config.kstatConfigs,
            SuSFSConfig.KEY_ADD_KSTAT_PATHS to config.addKstatPaths,
            SuSFSConfig.KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS to config.hideSusMountsForAllProcs,
            SuSFSConfig.KEY_ENABLE_HIDE_BL to config.enableHideBl,
            SuSFSConfig.KEY_ENABLE_CLEANUP_RESIDUE to config.enableCleanupResidue,
            SuSFSConfig.KEY_ENABLE_AVC_LOG_SPOOFING to config.enableAvcLogSpoofing
        )
    }

    private fun generateBackupFileName(): String {
        val df = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "SuSFS_Config_${df.format(Date())}${SuSFSConfig.BACKUP_FILE_EXTENSION}"
    }

    private fun getDeviceInfo(): String =
        try { "${Build.MANUFACTURER} ${Build.MODEL} (${Build.VERSION.RELEASE})" } catch (_: Exception) { "Unknown Device" }

    private suspend fun showToast(context: Context, message: String) = withContext(Dispatchers.Main) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    suspend fun createBackup(context: Context, backupFilePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupData = BackupData(
                version = getSuSFSVersion(),
                timestamp = System.currentTimeMillis(),
                deviceInfo = getDeviceInfo(),
                configurations = getAllConfigurations()
            )
            val f = File(backupFilePath)
            f.parentFile?.mkdirs()
            f.writeText(backupData.toJson())
            showToast(context, context.getString(R.string.susfs_backup_success, f.name))
            true
        } catch (e: Exception) {
            showToast(context, context.getString(R.string.susfs_backup_failed, e.message ?: "Unknown error"))
            false
        }
    }

    suspend fun restoreFromBackup(context: Context, backupFilePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val f = File(backupFilePath)
            if (!f.exists()) {
                showToast(context, context.getString(R.string.susfs_backup_file_not_found))
                return@withContext false
            }
            require(f.length() in 1..MAX_BACKUP_BYTES) { "Invalid SuSFS backup size" }
            val obj = org.json.JSONObject(f.readText())
            val confObj = obj.getJSONObject("configurations")
            val configurations = mutableMapOf<String, Any>()
            confObj.keys().forEach { key ->
                val value = confObj.get(key)
                configurations[key] = when (value) {
                    is org.json.JSONArray -> {
                        val set = mutableSetOf<String>()
                        for (i in 0 until value.length()) set.add(value.getString(i))
                        set
                    }
                    else -> value
                }
            }

            val previous = SuSFSConfig.dump()
            val restored = serializeConfigurations(configurations)
            check(SuSFSConfig.replace(restored)) { "Failed to replace SuSFS configuration" }

            val moduleUpdated = updateModuleForConfig(restored)
            if (!moduleUpdated) {
                SuSFSConfig.replace(previous)
                updateModuleForConfig(previous)
                error("Failed to update the SuSFS auto-start module")
            }

            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            showToast(context, context.getString(
                R.string.susfs_restore_success,
                df.format(Date(obj.getLong("timestamp"))),
                obj.getString("deviceInfo")
            ))
            true
        } catch (e: Exception) {
            showToast(context, context.getString(R.string.susfs_restore_failed, e.message ?: "Unknown error"))
            false
        }
    }

    private fun serializeConfigurations(configurations: Map<String, Any>): Map<String, String> =
        configurations.mapValues { (key, value) ->
            when (value) {
                is String -> value
                is Boolean -> value.toString()
                is Set<*> -> {
                    val separator = if (key == SuSFSConfig.KEY_KSTAT_CONFIGS) ";;" else ";"
                    val values = value.filterIsInstance<String>()
                    require(values.none { it.contains(separator) }) {
                        "Configuration value for $key contains reserved separator"
                    }
                    values.sorted().joinToString(separator)
                }
                else -> error("Unsupported configuration value for $key")
            }
        }

    private suspend fun updateModuleForConfig(configurations: Map<String, String>): Boolean {
        return if (configurations[SuSFSConfig.KEY_AUTO_START_ENABLED] == "true") {
            SuSFSCommands.updateMagiskModule()
        } else {
            SuSFSCommands.removeMagiskModule()
        }
    }

    suspend fun validateBackupFile(backupFilePath: String): BackupData? = withContext(Dispatchers.IO) {
        try {
            val f = File(backupFilePath)
            if (!f.exists()) return@withContext null
            if (f.length() !in 1..MAX_BACKUP_BYTES) return@withContext null
            val obj = org.json.JSONObject(f.readText())
            val confObj = obj.getJSONObject("configurations")
            val configurations = mutableMapOf<String, Any>()
            confObj.keys().forEach { key ->
                val value = confObj.get(key)
                configurations[key] = when (value) {
                    is org.json.JSONArray -> {
                        val set = mutableSetOf<String>()
                        for (i in 0 until value.length()) set.add(value.getString(i))
                        set
                    }
                    else -> value
                }
            }
            BackupData(
                version = obj.getString("version"),
                timestamp = obj.getLong("timestamp"),
                deviceInfo = obj.getString("deviceInfo"),
                configurations = configurations
            )
        } catch (_: Exception) { null }
    }

    fun getDefaultBackupFileName(): String = generateBackupFileName()
}
