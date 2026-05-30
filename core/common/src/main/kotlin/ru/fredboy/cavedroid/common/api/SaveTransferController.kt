package ru.fredboy.cavedroid.common.api

interface SaveTransferController {

    val isSupported: Boolean

    /**
     * Hand the user a zip archive to save or share. [onResult] is invoked with
     * `true` on success, `false` on failure or cancellation.
     */
    fun exportSave(
        suggestedFileName: String,
        zipBytes: ByteArray,
        onResult: (success: Boolean) -> Unit,
    )

    /**
     * Let the user pick a zip archive to import. [onResult] is invoked with the
     * file's bytes, or `null` if the user cancelled or an error occurred.
     */
    fun importSave(onResult: (zipBytes: ByteArray?) -> Unit)
}
