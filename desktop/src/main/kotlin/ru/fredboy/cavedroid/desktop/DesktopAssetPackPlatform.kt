package ru.fredboy.cavedroid.desktop

import com.badlogic.gdx.files.FileHandle
import ru.fredboy.cavedroid.common.api.AssetPackPlatform
import ru.fredboy.cavedroid.data.assets.pack.NativePackArchives
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

class DesktopAssetPackPlatform : AssetPackPlatform {
    override val isSupported = true

    override fun pickArchive(onResult: (Result<FileHandle?>) -> Unit) {
        Thread({
            val result = runCatching {
                val dialog = FileDialog(null as Frame?, "Import asset pack", FileDialog.LOAD)
                try {
                    // GTK's filename-filter callback needs an absolute initial directory.
                    dialog.directory = File(System.getProperty("user.home")).absolutePath
                    dialog.setFilenameFilter { _, name -> name.endsWith(".zip", ignoreCase = true) }
                    dialog.isVisible = true
                    val name = dialog.file ?: return@runCatching null
                    val source = File(dialog.directory, name)
                    val temporary = FileHandle(File.createTempFile("cavedroid-pack-", ".zip"))
                    try {
                        source.inputStream().use { NativePackArchives.copy(it, temporary) }
                        temporary
                    } catch (exception: Exception) {
                        temporary.delete()
                        throw exception
                    }
                } finally {
                    dialog.dispose()
                }
            }
            onResult(result)
        }, "asset-pack-dialog").apply { isDaemon = true }.start()
    }

    override fun extractArchive(archive: FileHandle, destination: FileHandle): String = NativePackArchives.extract(archive, destination)
}
