package ru.fredboy.cavedroid

import android.app.Activity
import android.content.Intent
import com.badlogic.gdx.files.FileHandle
import ru.fredboy.cavedroid.common.api.AssetPackPlatform
import ru.fredboy.cavedroid.data.assets.pack.NativePackArchives
import java.io.File

class AndroidAssetPackPlatform(private val activity: Activity) : AssetPackPlatform {
    override val isSupported = true
    private var pending: ((Result<FileHandle?>) -> Unit)? = null

    override fun pickArchive(onResult: (Result<FileHandle?>) -> Unit) {
        activity.runOnUiThread {
            if (pending != null) {
                onResult(Result.failure(IllegalStateException("An import is already in progress")))
                return@runOnUiThread
            }
            pending = onResult
            try {
                activity.startActivityForResult(
                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    },
                    REQUEST_IMPORT_PACK,
                )
            } catch (exception: Exception) {
                pending = null
                onResult(Result.failure(exception))
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_IMPORT_PACK) return false
        val callback = pending
        pending = null
        val uri = data?.data
        if (callback != null) {
            if (resultCode != Activity.RESULT_OK || uri == null) {
                callback(Result.success(null))
            } else {
                Thread({
                    callback(
                        runCatching {
                            val temporary = FileHandle(File.createTempFile("asset-pack-", ".zip", activity.cacheDir))
                            try {
                                requireNotNull(activity.contentResolver.openInputStream(uri)).use { NativePackArchives.copy(it, temporary) }
                                temporary
                            } catch (exception: Exception) {
                                temporary.delete()
                                throw exception
                            }
                        },
                    )
                }, "asset-pack-import").start()
            }
        }
        return true
    }

    override fun extractArchive(archive: FileHandle, destination: FileHandle): String = NativePackArchives.extract(archive, destination)

    private companion object {
        const val REQUEST_IMPORT_PACK = 0xE2
    }
}
