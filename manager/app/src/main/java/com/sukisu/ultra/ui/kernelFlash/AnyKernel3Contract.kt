package com.sukisu.ultra.ui.kernelFlash

import java.util.Locale

/**
 * Pure AnyKernel3 protocol rules shared by archive detection and the flashing worker.
 *
 * Keeping these rules outside Android classes makes changes to the supported archive contract
 * and generated shell code independently testable.
 */
object AnyKernel3Contract {
    const val UPDATE_BINARY_ENTRY = "META-INF/com/google/android/update-binary"

    private val setupBusyboxCall = Regex("(?m)^setup_bb;[\\t \\r]*$")

    fun inspectArchive(): ArchiveLayout = ArchiveLayout()

    fun isSupportedArchive(entryNames: Iterable<String>): Boolean = inspectArchive().apply {
        entryNames.forEach(::record)
    }.isSupported

    class ArchiveLayout internal constructor() {
        private var hasAnykernelScript = false
        private var hasToolsFile = false
        private var hasUpdateBinary = false

        fun record(entryName: String) {
            val normalizedEntryName = entryName.lowercase(Locale.ROOT)
            when (normalizedEntryName) {
                "anykernel.sh" -> hasAnykernelScript = true
                UPDATE_BINARY_ENTRY.lowercase(Locale.ROOT) -> hasUpdateBinary = true
                else -> if (normalizedEntryName.startsWith("tools/") && normalizedEntryName != "tools/") {
                    hasToolsFile = true
                }
            }
        }

        val isSupported: Boolean
            get() = hasAnykernelScript && hasToolsFile && hasUpdateBinary
    }

    fun patchUpdateBinary(script: String, preparationScript: String): String {
        require(script.startsWith("#!")) { "Invalid AnyKernel3 update-binary script" }
        val match = setupBusyboxCall.find(script)
            ?: throw IllegalArgumentException("Unsupported AnyKernel3 update-binary: setup hook not found")
        return script.substring(0, match.range.first) + preparationScript + script.substring(match.range.first)
    }

    fun buildPreparationScript(
        tools: AnyKernel3ToolPaths,
        kpmMode: AnyKernel3KpmMode
    ): String = buildString {
        appendLine("# MakoSU AnyKernel3 preparation")
        append("cp -f ")
        append(shellQuote(tools.mkbootfs))
        append(" \"\$AKHOME/tools/mkbootfs\" || abort \"Failed to prepare MakoSU mkbootfs.\"\n")

        if (kpmMode != AnyKernel3KpmMode.NONE) {
            val kptools = requireNotNull(tools.kptools) { "KPM requires kptools" }
            val kpimg = requireNotNull(tools.kpimg) { "KPM requires kpimg" }
            append("makosu_image=\$(find \"\$AKHOME\" -type f -name 'Image*' | head -n 1)\n")
            append("[ -n \"\$makosu_image\" ] || abort \"KPM requested but no Image file was found.\"\n")
            append("chmod 755 ")
            append(shellQuote(kptools))
            append(" || abort \"Unable to execute KPM tools.\"\n")
            append(shellQuote(kptools))
            append(' ')
            append(kpmMode.argument)
            append(" -s 123 -i \"\$makosu_image\" -k ")
            append(shellQuote(kpimg))
            append(" -o \"\$makosu_image.makosu\" || abort \"KPM patch failed.\"\n")
            append("[ -s \"\$makosu_image.makosu\" ] || abort \"KPM patch produced no output.\"\n")
            append("mv -f \"\$makosu_image.makosu\" \"\$makosu_image\" || abort \"Unable to replace patched Image.\"\n")
            append("rm -f \"\$makosu_image.makosu\"\n")
        }
    }

    fun slotSelectionEnvironment(targetSlot: String?, activeSlot: String?): String? {
        val normalizedTarget = targetSlot?.let(::requireSlot) ?: return null
        val normalizedActive = activeSlot?.let(::normalizeSlot)
            ?.takeIf { it == "a" || it == "b" }
            ?: throw IllegalArgumentException("Unable to determine the active A/B slot")
        return if (normalizedTarget == normalizedActive) null else "inactive"
    }

    fun buildFlashCommand(
        postInstallDirectory: String,
        updateBinary: String,
        archive: String,
        slotSelection: String?
    ): String = buildString {
        append("POSTINSTALL=")
        append(shellQuote(postInstallDirectory))
        append(' ')
        if (slotSelection != null) {
            append("SLOT_SELECT=")
            append(slotSelection)
            append(' ')
        }
        append("sh ")
        append(shellQuote(updateBinary))
        append(" 3 1 ")
        append(shellQuote(archive))
    }

    fun mkbootfsAsset(kernelVersion: String): String {
        val versionParts = kernelVersion.split('.')
        val major = versionParts.getOrNull(0)?.toIntOrNull()
        val minor = versionParts.getOrNull(1)?.toIntOrNull()
        return if (major != null && minor != null && (major < 5 || major == 5 && minor <= 10)) {
            "5_10"
        } else {
            "5_15+"
        }
    }

    fun shellQuote(value: String): String = "'${value.replace("'", "'\"'\"'")}'"

    private fun requireSlot(slot: String): String {
        val normalized = normalizeSlot(slot)
        require(normalized == "a" || normalized == "b") { "Invalid target slot: $slot" }
        return normalized
    }

    private fun normalizeSlot(slot: String): String = slot.trim().removePrefix("_").lowercase(Locale.ROOT)
}

data class AnyKernel3ToolPaths(
    val mkbootfs: String,
    val kptools: String? = null,
    val kpimg: String? = null
) {
    init {
        require(mkbootfs.isNotBlank()) { "mkbootfs path must not be blank" }
    }
}

enum class AnyKernel3KpmMode(val argument: String) {
    NONE(""),
    PATCH("-p"),
    UNDO("-u")
}
