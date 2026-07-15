package com.sukisu.ultra.ui.screen.susfs.util

import android.annotation.SuppressLint
import com.sukisu.ultra.ui.util.getKsuDaemonPath
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object SuSFSConfig {
    // Config keys (must match Rust susfs_config.rs)
    const val KEY_UNAME_VALUE = "uname_value"
    const val KEY_BUILD_TIME_VALUE = "build_time_value"
    const val KEY_AUTO_START_ENABLED = "auto_start_enabled"
    const val KEY_SUS_PATHS = "sus_paths"
    const val KEY_SUS_LOOP_PATHS = "sus_loop_paths"
    const val KEY_SUS_MAPS = "sus_maps"
    const val KEY_ENABLE_LOG = "enable_log"
    const val KEY_EXECUTE_IN_POST_FS_DATA = "execute_in_post_fs_data"
    const val KEY_KSTAT_CONFIGS = "kstat_configs"
    const val KEY_ADD_KSTAT_PATHS = "add_kstat_paths"
    const val KEY_HIDE_SUS_MOUNTS_FOR_ALL_PROCS = "hide_sus_mounts_for_all_procs"
    const val KEY_ENABLE_CLEANUP_RESIDUE = "enable_cleanup_residue"
    const val KEY_ENABLE_HIDE_BL = "enable_hide_bl"
    const val KEY_ENABLE_AVC_LOG_SPOOFING = "enable_avc_log_spoofing"

    // Defaults
    const val DEFAULT_UNAME = "default"
    const val DEFAULT_BUILD_TIME = "default"

    @SuppressLint("SdCardPath")
    const val DEFAULT_ANDROID_DATA_PATH = "/sdcard/Android/data"
    const val BACKUP_FILE_EXTENSION = ".susfs_backup"
    const val MEDIA_DATA_PATH = "/data/media/0/Android/data"
    const val CGROUP_BASE_PATH = "/sys/fs/cgroup"

    private suspend fun configGet(key: String): String = withContext(Dispatchers.IO) {
        val result = Shell.getShell().newJob()
            .add("${getKsuDaemonPath()} susfs config get $key")
            .exec()
        check(result.isSuccess) {
            result.err.joinToString("\n").ifBlank { "Failed to read SuSFS config key: $key" }
        }
        result.out.joinToString("\n").trim()
    }

    suspend fun get(key: String): String = configGet(key)

    suspend fun dump(): Map<String, String> = withContext(Dispatchers.IO) {
        val result = Shell.getShell().newJob()
            .add("${getKsuDaemonPath()} susfs config dump")
            .exec()
        check(result.isSuccess) {
            result.err.joinToString("\n").ifBlank { "Failed to read SuSFS configuration" }
        }

        val json = JSONObject(result.out.joinToString("\n"))
        buildMap {
            json.keys().forEach { key -> put(key, json.getString(key)) }
        }
    }

    suspend fun replace(values: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
        val json = JSONObject(values).toString()
        Shell.getShell().newJob()
            .add("${getKsuDaemonPath()} susfs config replace ${shellQuote(json)}")
            .exec()
            .isSuccess
    }

    suspend fun reset(): Boolean = withContext(Dispatchers.IO) {
        Shell.getShell().newJob()
            .add("${getKsuDaemonPath()} susfs config reset")
            .exec()
            .isSuccess
    }

    suspend fun configSet(key: String, value: String) = withContext(Dispatchers.IO) {
        val result = Shell.getShell().newJob()
            .add("${getKsuDaemonPath()} susfs config set $key ${shellQuote(value)}")
            .exec()
        check(result.isSuccess) {
            result.err.joinToString("\n").ifBlank { "Failed to save SuSFS config key: $key" }
        }
    }

    suspend fun set(key: String, value: String) = configSet(key, value)

    suspend fun configSetMulti(key: String, values: Set<String>, separator: String) = withContext(Dispatchers.IO) {
        require(values.none { it.contains(separator) }) { "SuSFS value contains reserved separator: $separator" }
        val raw = values.sorted().joinToString(separator)
        configSet(key, raw)
    }

    suspend fun setMulti(key: String, values: Set<String>, separator: String) =
        configSetMulti(key, values, separator)

    private suspend fun configGetMulti(key: String, separator: String = ";"): Set<String> = withContext(Dispatchers.IO) {
        val raw = configGet(key)
        if (raw.isBlank()) emptySet() else raw.split(separator).filter { it.isNotBlank() }.toSet()
    }

    suspend fun getMulti(key: String, separator: String = ";"): Set<String> = configGetMulti(key, separator)

    fun shellQuote(value: String): String = "'${value.replace("'", "'\\''")}'"

    fun isValidMultiValue(value: String, separator: String = ";"): Boolean =
        value.isNotBlank() && !value.contains(separator)

    fun isDefaultSpoofValue(value: String): Boolean = value.isBlank() || value == DEFAULT_UNAME

}
