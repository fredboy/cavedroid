package ru.fredboy.cavedroid.desktop

import ru.fredboy.cavedroid.common.api.SaveTransferController
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

class DesktopSaveTransferController : SaveTransferController {

    override val isSupported: Boolean = true

    override fun exportSave(
        suggestedFileName: String,
        zipBytes: ByteArray,
        onResult: (success: Boolean) -> Unit,
    ) {
        // FileDialog is modal and blocking; run it off the render thread.
        runOnDialogThread {
            val success = runCatching {
                val dialog = FileDialog(null as Frame?, "Export world", FileDialog.SAVE)
                dialog.file = suggestedFileName
                dialog.isVisible = true

                val dir = dialog.directory
                val name = dialog.file
                if (dir == null || name == null) {
                    return@runCatching false
                }
                File(dir, name).writeBytes(zipBytes)
                true
            }.getOrDefault(false)

            onResult(success)
        }
    }

    override fun importSave(onResult: (zipBytes: ByteArray?) -> Unit) {
        runOnDialogThread {
            val bytes = runCatching {
                val dialog = FileDialog(null as Frame?, "Import world", FileDialog.LOAD)
                dialog.setFilenameFilter { _, name -> name.endsWith(".cvdworld", ignoreCase = true) }
                dialog.isVisible = true

                val dir = dialog.directory
                val name = dialog.file
                if (dir == null || name == null) {
                    return@runCatching null
                }
                File(dir, name).readBytes()
            }.getOrNull()

            onResult(bytes)
        }
    }

    private fun runOnDialogThread(block: () -> Unit) {
        Thread(block, "save-transfer-dialog").apply { isDaemon = true }.start()
    }
}
