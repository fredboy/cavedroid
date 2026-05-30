package ru.fredboy.cavedroid

import android.app.Activity
import android.content.Intent
import ru.fredboy.cavedroid.common.api.SaveTransferController

class AndroidSaveTransferController(
    private val activity: Activity,
) : SaveTransferController {

    override val isSupported: Boolean = true

    private var pendingExportBytes: ByteArray? = null
    private var pendingExportCallback: ((Boolean) -> Unit)? = null
    private var pendingImportCallback: ((ByteArray?) -> Unit)? = null

    override fun exportSave(
        suggestedFileName: String,
        zipBytes: ByteArray,
        onResult: (success: Boolean) -> Unit,
    ) {
        pendingExportBytes = zipBytes
        pendingExportCallback = onResult

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_ZIP
            putExtra(Intent.EXTRA_TITLE, suggestedFileName)
        }
        activity.startActivityForResult(intent, REQUEST_EXPORT)
    }

    override fun importSave(onResult: (zipBytes: ByteArray?) -> Unit) {
        pendingImportCallback = onResult

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            // Some providers tag .zip as octet-stream, so accept everything and
            // let the import parser reject anything that isn't a valid save.
            type = "*/*"
        }
        activity.startActivityForResult(intent, REQUEST_IMPORT)
    }

    /** Returns true if the result was consumed by this controller. */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        when (requestCode) {
            REQUEST_EXPORT -> {
                val callback = pendingExportCallback
                val bytes = pendingExportBytes
                pendingExportCallback = null
                pendingExportBytes = null

                val uri = data?.data
                val success = if (resultCode == Activity.RESULT_OK && uri != null && bytes != null) {
                    runCatching {
                        activity.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        true
                    }.getOrDefault(false)
                } else {
                    false
                }
                callback?.invoke(success)
                return true
            }

            REQUEST_IMPORT -> {
                val callback = pendingImportCallback
                pendingImportCallback = null

                val uri = data?.data
                val bytes = if (resultCode == Activity.RESULT_OK && uri != null) {
                    runCatching {
                        activity.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }.getOrNull()
                } else {
                    null
                }
                callback?.invoke(bytes)
                return true
            }
        }
        return false
    }

    companion object {
        private const val MIME_ZIP = "application/octet-stream"
        private const val REQUEST_EXPORT = 0xE0
        private const val REQUEST_IMPORT = 0xE1
    }
}
