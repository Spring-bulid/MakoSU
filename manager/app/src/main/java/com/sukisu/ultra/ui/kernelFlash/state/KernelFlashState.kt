package com.sukisu.ultra.ui.kernelFlash.state

import android.content.Context
import android.net.Uri
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.kernelFlash.AnyKernel3Contract
import com.sukisu.ultra.ui.kernelFlash.AnyKernel3KpmMode
import com.sukisu.ultra.ui.kernelFlash.AnyKernel3ToolPaths
import com.sukisu.ultra.ui.kernelFlash.util.AssetsUtil
import com.sukisu.ultra.ui.util.getRootShell
import com.sukisu.ultra.ui.util.install
import com.sukisu.ultra.ui.util.rootAvailable
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

data class FlashState(
    val isFlashing: Boolean = false,
    val isCompleted: Boolean = false,
    val progress: Float = 0f,
    val currentStep: String = "",
    val logs: List<String> = emptyList(),
    val error: String = ""
)

class HorizonKernelState {
    private val _state = MutableStateFlow(FlashState())
    val state: StateFlow<FlashState> = _state.asStateFlow()
    private val logBuffer = ArrayDeque<String>(MAX_LOG_LINES)

    fun updateProgress(progress: Float) {
        _state.update { it.copy(progress = progress) }
    }

    fun updateStep(step: String) {
        _state.update { it.copy(currentStep = step) }
    }

    fun addLog(log: String) = addLogs(listOf(log))

    fun addLogs(logs: Iterable<String>) {
        val snapshot = synchronized(logBuffer) {
            logs.forEach { log ->
                if (log.isNotBlank()) {
                    if (logBuffer.size == MAX_LOG_LINES) logBuffer.removeFirst()
                    logBuffer.addLast(log)
                }
            }
            logBuffer.toList()
        }
        _state.update { it.copy(logs = snapshot) }
    }

    fun setError(error: String) {
        _state.update { it.copy(isFlashing = false, error = error) }
    }

    fun startFlashing() {
        synchronized(logBuffer) { logBuffer.clear() }
        _state.update {
            it.copy(
                isFlashing = true,
                isCompleted = false,
                progress = 0f,
                currentStep = "Preparing...",
                logs = emptyList(),
                error = ""
            )
        }
    }

    fun completeFlashing() {
        _state.update { it.copy(isFlashing = false, isCompleted = true, progress = 1f) }
    }

    private companion object {
        const val MAX_LOG_LINES = 500
    }
}

/**
 * Runs a standard AnyKernel3 update-binary in a disposable application-cache session.
 *
 * AnyKernel3 intentionally owns the extraction of its archive. The manager only extracts and
 * patches update-binary so the original ZIP metadata, symlinks, and tool permissions remain
 * untouched.
 */
class HorizonKernelWorker(
    private val context: Context,
    private val state: HorizonKernelState,
    private val archiveUri: Uri,
    private val slot: String? = null,
    private val kpmPatchEnabled: Boolean = false,
    private val kpmUndoPatch: Boolean = false
) : Thread() {
    private lateinit var sessionDir: File
    private lateinit var inputZip: File
    private lateinit var updateBinary: File
    private lateinit var mkbootfs: File
    private lateinit var kptools: File
    private lateinit var kpimg: File

    override fun run() {
        state.startFlashing()
        state.updateStep(context.getString(R.string.horizon_preparing))

        try {
            state.updateStep(context.getString(R.string.horizon_cleaning_files))
            state.updateProgress(0.1f)
            prepareSession()

            if (!rootAvailable()) {
                throw IOException(context.getString(R.string.root_required))
            }

            state.updateStep(context.getString(R.string.horizon_copying_files))
            state.updateProgress(0.2f)
            copyArchive(archiveUri)

            state.updateStep(context.getString(R.string.horizon_extracting_tool))
            state.updateProgress(0.4f)
            extractUpdateBinary()

            state.updateStep(context.getString(R.string.horizon_patching_script))
            state.updateProgress(0.6f)
            prepareToolsAndPatchUpdateBinary()

            state.updateStep(context.getString(R.string.horizon_flashing))
            state.updateProgress(0.7f)
            flash()

            try {
                install()
            } catch (e: Exception) {
                state.addLog("ksud update skipped: ${e.message}")
            }

            state.updateStep(context.getString(R.string.horizon_flash_complete_status))
            state.completeFlashing()
        } catch (e: Exception) {
            state.setError(e.message ?: context.getString(R.string.horizon_unknown_error))
        } finally {
            cleanupSession()
        }
    }

    private fun prepareSession() {
        sessionDir = File(context.cacheDir, SESSION_DIRECTORY)
        if (sessionDir.exists() && !sessionDir.deleteRecursively()) {
            throw IOException("Unable to clear previous AnyKernel3 session")
        }
        if (!sessionDir.mkdirs()) {
            throw IOException("Unable to create AnyKernel3 session")
        }

        inputZip = File(sessionDir, "kernel.zip")
        updateBinary = File(sessionDir, "update-binary")
        mkbootfs = File(sessionDir, "mkbootfs")
        kptools = File(sessionDir, "kptools")
        kpimg = File(sessionDir, "kpimg")
    }

    private fun cleanupSession() {
        if (::sessionDir.isInitialized && sessionDir.exists() && !sessionDir.deleteRecursively()) {
            state.addLog("Warning: unable to remove temporary AnyKernel3 files")
        }
    }

    private fun copyArchive(sourceUri: Uri) {
        val copiedBytes = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(inputZip).use { output -> input.copyTo(output) }
        } ?: throw IOException(context.getString(R.string.horizon_copy_failed))

        if (copiedBytes <= 0L || !inputZip.isFile || inputZip.length() <= 0L) {
            throw IOException(context.getString(R.string.horizon_copy_failed))
        }
    }

    private fun extractUpdateBinary() {
        ZipFile(inputZip).use { archive ->
            val entry = archive.getEntry(AnyKernel3Contract.UPDATE_BINARY_ENTRY)
                ?: throw IOException("Invalid AnyKernel3 archive: missing ${AnyKernel3Contract.UPDATE_BINARY_ENTRY}")

            archive.getInputStream(entry).use { input ->
                FileOutputStream(updateBinary).use { output -> input.copyTo(output) }
            }
        }

        if (!updateBinary.isFile || updateBinary.length() == 0L) {
            throw IOException("Invalid AnyKernel3 archive: update-binary is empty")
        }
        updateBinary.setExecutable(true, false)
    }

    private fun prepareToolsAndPatchUpdateBinary() {
        val kernelVersion = ShellUtils.fastCmd(getRootShell(), "cat /proc/version")
        val version = KERNEL_VERSION.find(kernelVersion)?.value.orEmpty()
        val toolName = AnyKernel3Contract.mkbootfsAsset(version)

        AssetsUtil.exportFiles(context, "$toolName-mkbootfs", mkbootfs.absolutePath)
        if (!mkbootfs.isFile || mkbootfs.length() == 0L) {
            throw IOException("Unable to prepare mkbootfs")
        }

        val kpmMode = getKpmMode()
        if (kpmMode != AnyKernel3KpmMode.NONE) {
            state.updateStep(context.getString(R.string.kpm_preparing_tools))
            state.updateProgress(0.5f)
            AssetsUtil.exportFiles(context, "kptools", kptools.absolutePath)
            AssetsUtil.exportFiles(context, "kpimg", kpimg.absolutePath)
            if (!kptools.isFile || !kpimg.isFile || kptools.length() == 0L || kpimg.length() == 0L) {
                throw IOException("Unable to prepare KPM tools")
            }
            state.updateStep(
                if (kpmUndoPatch) context.getString(R.string.kpm_undoing_patch)
                else context.getString(R.string.kpm_applying_patch)
            )
            state.updateProgress(0.55f)
        }

        state.addLog(
            "${context.getString(R.string.kernel_version_log, version)} " +
                context.getString(R.string.tool_version_log, toolName)
        )

        val script = updateBinary.readText(Charsets.UTF_8)
        val tools = AnyKernel3ToolPaths(
            mkbootfs = mkbootfs.absolutePath,
            kptools = kptools.absolutePath,
            kpimg = kpimg.absolutePath
        )
        val patchedScript = try {
            AnyKernel3Contract.patchUpdateBinary(
                script = script,
                preparationScript = AnyKernel3Contract.buildPreparationScript(tools, kpmMode)
            )
        } catch (e: IllegalArgumentException) {
            throw IOException(e.message, e)
        }
        updateBinary.writeText(patchedScript, Charsets.UTF_8)
        updateBinary.setExecutable(true, false)
    }

    private fun flash() {
        val activeSlot = if (slot == null) null else {
            ShellUtils.fastCmd(getRootShell(), "getprop ro.boot.slot_suffix")
        }
        val slotMode = try {
            AnyKernel3Contract.slotSelectionEnvironment(slot, activeSlot)
        } catch (e: IllegalArgumentException) {
            throw IOException(e.message, e)
        }
        val command = AnyKernel3Contract.buildFlashCommand(
            postInstallDirectory = sessionDir.absolutePath,
            updateBinary = updateBinary.absolutePath,
            archive = inputZip.absolutePath,
            slotSelection = slotMode
        )

        val output = ArrayList<String>()
        val errors = ArrayList<String>()
        val result = getRootShell().newJob().add(command).to(output, errors).exec()
        recordFlashOutput(output + errors)

        if (!result.isSuccess) {
            val details = (errors + output)
                .asReversed()
                .firstOrNull { it.isNotBlank() && !it.trim().equals("ui_print", ignoreCase = true) }
                ?.trim()
            val message = context.getString(R.string.flash_failed_message)
            throw IOException(if (details == null) message else "$message: $details")
        }
    }

    private fun recordFlashOutput(lines: List<String>) {
        val messages = lines.mapNotNull { line ->
            line.removePrefix("ui_print").trim().takeIf { it.isNotEmpty() }
        }
        if (messages.isEmpty()) return

        state.addLogs(messages)
        messages.forEach { message ->
            when {
                message.contains("done", ignoreCase = true) || message.contains("complete", ignoreCase = true) -> {
                    state.updateProgress(0.95f)
                }
                message.contains("install", ignoreCase = true) -> state.updateProgress(0.85f)
                message.contains("extract", ignoreCase = true) -> state.updateProgress(0.75f)
            }
        }
    }

    private fun getKpmMode(): AnyKernel3KpmMode = when {
        kpmUndoPatch -> AnyKernel3KpmMode.UNDO
        kpmPatchEnabled -> AnyKernel3KpmMode.PATCH
        else -> AnyKernel3KpmMode.NONE
    }

    private companion object {
        const val SESSION_DIRECTORY = "anykernel3-flash"
        val KERNEL_VERSION = """\d+\.\d+\.\d+""".toRegex()
    }
}
