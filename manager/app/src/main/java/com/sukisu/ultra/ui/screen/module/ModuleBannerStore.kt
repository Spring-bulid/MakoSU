package com.sukisu.ultra.ui.screen.module

/**
 * Module banner I/O — FolkPatch APM strategy with a more reliable shell fallback.
 *
 * Many modules store banners that SuFile cannot open (special mounts / SELinux / large files).
 * Primary path: shell `base64` / `find` / module.prop; SuFile is secondary.
 */

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.sukisu.ultra.ui.util.getRootShell
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import java.io.File
private const val TAG = "ModuleBanner"
private const val BANNER_DIR_NAME = "folk_banners"
private const val LEGACY_BANNER_NAME = "FolkBanner"
private const val MAX_BYTES = 12 * 1024 * 1024

data class ModuleBannerInfo(
    val bytes: ByteArray? = null,
    val url: String? = null,
) {
    val hasContent: Boolean
        get() = (bytes != null && bytes.isNotEmpty()) || !url.isNullOrBlank()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModuleBannerInfo) return false
        if (url != other.url) return false
        if (bytes === other.bytes) return true
        if (bytes == null || other.bytes == null) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = url?.hashCode() ?: 0
        result = 31 * result + (bytes?.contentHashCode() ?: 0)
        return result
    }
}

object ModuleBannerStore {

    /** Serialize multi-module shell IO against shared root shell. */
    private val shellIoLock = Any()

    private fun sanitizeBannerKey(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9._-]"), "_")

    private fun localBannerFile(context: Context, moduleId: String): File {
        val dir = File(context.filesDir, BANNER_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, sanitizeBannerKey(moduleId))
    }

    fun hasLocal(context: Context, moduleId: String): Boolean =
        localBannerFile(context, moduleId).let { it.isFile && it.length() > 0L }

    fun readLocal(context: Context, moduleId: String): ByteArray? = runCatching {
        val file = localBannerFile(context, moduleId)
        if (file.exists()) file.readBytes().takeIf { it.isNotEmpty() } else null
    }.getOrNull()

    fun writeLocal(context: Context, moduleId: String, uri: Uri): ByteArray? {
        val data = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        if (data.isEmpty() || data.size > MAX_BYTES) return null
        localBannerFile(context, moduleId).outputStream().use { it.write(data) }
        return data
    }

    fun clearLocal(context: Context, moduleId: String): Boolean {
        val file = localBannerFile(context, moduleId)
        return !file.exists() || file.delete()
    }

    private fun suFile(path: String, shell: Shell): SuFile =
        SuFile(path).apply { this.shell = shell }

    /**
     * Read binary file via shell base64 — more reliable than SuFile on some modules.
     */
    private fun readBytesViaShell(shell: Shell, path: String): ByteArray? {
        // Quote path safely for shell
        val quoted = path.replace("'", "'\\''")
        val out = ArrayList<String>()
        val err = ArrayList<String>()
        // Prefer GNU base64 -w 0; fall back to plain base64 (Toybox wraps lines — still OK)
        val result = shell.newJob()
            .add("if [ -f '$quoted' ] && [ -r '$quoted' ]; then base64 -w 0 '$quoted' 2>/dev/null || base64 '$quoted' 2>/dev/null; fi")
            .to(out, err)
            .exec()
        if (!result.isSuccess && out.isEmpty()) return null
        val b64 = out.joinToString("").replace("\n", "").replace("\r", "").trim()
        if (b64.isEmpty()) return null
        return runCatching {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            if (bytes.isNotEmpty() && bytes.size <= MAX_BYTES) bytes else null
        }.getOrNull()
    }

    private fun readBytesSuFile(shell: Shell, path: String): ByteArray? = runCatching {
        val file = suFile(path, shell)
        if (!file.exists()) return@runCatching null
        file.newInputStream().use { it.readBytes() }
            .takeIf { it.isNotEmpty() && it.size <= MAX_BYTES }
    }.getOrNull()

    private fun readBytes(shell: Shell, path: String): ByteArray? {
        // Shell first (manager reliability), SuFile second
        return readBytesViaShell(shell, path) ?: readBytesSuFile(shell, path)
    }

    private fun shellCmd(shell: Shell, cmd: String): String {
        val out = ArrayList<String>()
        shell.newJob().add(cmd).to(out, null).exec()
        return out.joinToString("\n").trim()
    }

    fun resolveModuleDir(rootShell: Shell, moduleId: String): String {
        val safeId = moduleId.replace("'", "'\\''")
        val defaultDir = "/data/adb/modules/$moduleId"
        return runCatching {
            // Fast path: id == directory name
            if (shellCmd(rootShell, "[ -d '$defaultDir' ] && echo ok") == "ok") {
                return@runCatching defaultDir
            }
            // Slow path: match module.prop id= (dir name may differ)
            val script =
                "for d in /data/adb/modules/*/; do " +
                    "[ -f \"\${d}module.prop\" ] || continue; " +
                    "id=\$(grep -m1 '^id=' \"\${d}module.prop\" 2>/dev/null | cut -d= -f2- | tr -d '\\r'); " +
                    "if [ \"\$id\" = '$safeId' ]; then echo \"\${d%/}\"; break; fi; " +
                    "done"
            shellCmd(rootShell, script)
                .lineSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("/") }
                ?: defaultDir
        }.getOrDefault(defaultDir)
    }

    private fun readModulePropBanner(shell: Shell, resolvedDir: String): String? {
        val quoted = resolvedDir.replace("'", "'\\''")
        val line = shellCmd(
            shell,
            "grep -m1 '^banner=' '$quoted/module.prop' 2>/dev/null | cut -d= -f2- | tr -d '\\r'",
        ).trim()
        return line.takeIf { it.isNotEmpty() }
    }

    /**
     * Discover candidate banner paths under the module dir.
     */
    private fun discoverCandidates(shell: Shell, resolvedDir: String, propBanner: String?): List<String> {
        val quoted = resolvedDir.replace("'", "'\\''")
        val list = linkedSetOf<String>()
        if (!propBanner.isNullOrEmpty() && !propBanner.startsWith("http", ignoreCase = true)) {
            list += if (propBanner.startsWith("/")) propBanner else "$resolvedDir/$propBanner"
        }
        // Standard Folk names
        listOf("banner", "banner.png", "banner.jpg", "banner.jpeg", "banner.webp", "banner.gif", LEGACY_BANNER_NAME)
            .forEach { list += "$resolvedDir/$it" }
        // Find any *banner* file (depth 2) — covers modules that put assets/banner.webp etc.
        val found = shellCmd(
            shell,
            "find '$quoted' -maxdepth 2 -type f \\( -iname 'banner' -o -iname 'banner.*' -o -iname 'FolkBanner' -o -iname '*banner*.png' -o -iname '*banner*.jpg' -o -iname '*banner*.webp' \\) 2>/dev/null | head -n 12",
        )
        found.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("/") }
            .forEach { list += it }
        return list.toList()
    }

    /**
     * local folk_banners → prop http → disk candidates (shell+SuFile)
     * @param allowCustom when false, skip app-local custom banners (Folk isFolkBannerEnabled)
     */
    fun loadFromDisk(
        context: Context,
        moduleId: String,
        allowCustom: Boolean = true,
    ): ModuleBannerInfo? {
        // 1) App-local custom banner (no root)
        if (allowCustom) {
            val folk = readLocal(context, moduleId)
            if (folk != null) return ModuleBannerInfo(folk, null)
        }

        return synchronized(shellIoLock) {
            try {
                val rootShell = getRootShell(true)
                val resolvedDir = resolveModuleDir(rootShell, moduleId)
                Log.d(TAG, "load id=$moduleId dir=$resolvedDir")

                val propBanner = readModulePropBanner(rootShell, resolvedDir)
                if (!propBanner.isNullOrEmpty() && propBanner.startsWith("http", ignoreCase = true)) {
                    return@synchronized ModuleBannerInfo(null, propBanner)
                }

                val candidates = discoverCandidates(rootShell, resolvedDir, propBanner)
                for (path in candidates) {
                    val bytes = readBytes(rootShell, path) ?: continue
                    // Soft-validate as image; still accept if decoder fails (some webp)
                    if (isLikelyImage(bytes) || bytes.size > 64) {
                        Log.d(TAG, "loaded banner id=$moduleId path=$path size=${bytes.size}")
                        return@synchronized ModuleBannerInfo(bytes, null)
                    }
                }
                Log.d(TAG, "no banner for id=$moduleId candidates=${candidates.size}")
                null
            } catch (e: Exception) {
                Log.w(TAG, "loadFromDisk id=$moduleId failed: ${e.message}")
                null
            }
        }
    }

    fun isLikelyImage(bytes: ByteArray): Boolean {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        return opts.outWidth > 0 && opts.outHeight > 0
    }
}
