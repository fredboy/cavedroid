package ru.fredboy.cavedroid.common.api

object NoOpSaveTransferController : SaveTransferController {

    override val isSupported: Boolean = false

    override fun exportSave(
        suggestedFileName: String,
        zipBytes: ByteArray,
        onResult: (success: Boolean) -> Unit,
    ) = onResult(false)

    override fun importSave(onResult: (zipBytes: ByteArray?) -> Unit) = onResult(null)
}
