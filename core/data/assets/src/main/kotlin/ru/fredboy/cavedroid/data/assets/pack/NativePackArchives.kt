package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.files.FileHandle
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/** Referenced only by desktop and Android platform adapters. */
object NativePackArchives {
    fun copy(source: InputStream, destination: FileHandle) {
        try {
            destination.write(false).use { output ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = source.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= PackFormat.MAX_ARCHIVE_BYTES) { "ZIP exceeds 256 MiB" }
                    output.write(buffer, 0, count)
                }
            }
        } catch (exception: Exception) {
            destination.delete()
            throw exception
        }
    }

    fun extract(archive: FileHandle, destination: FileHandle): String {
        require(archive.length() in 1..PackFormat.MAX_ARCHIVE_BYTES) { "Invalid ZIP size" }
        val entryCount = ZipFile(archive.file()).use { it.size() }
        require(entryCount in 1..PackFormat.MAX_ENTRIES) { "Invalid ZIP entry count" }
        val digest = MessageDigest.getInstance("SHA-256")
        archive.read().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        val names = HashSet<String>()
        var total = 0L
        try {
            ZipInputStream(archive.read()).use { zip ->
                val buffer = ByteArray(8192)
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val name = PackFormat.path(entry.name.removeSuffix("/"))
                    require(names.add(name) && names.size <= PackFormat.MAX_ENTRIES) { "Duplicate entry or too many ZIP entries: $name" }
                    val target = destination.child(name)
                    require(target.file().canonicalPath.startsWith(destination.file().canonicalPath + java.io.File.separator)) {
                        "ZIP entry escapes pack directory"
                    }
                    if (entry.isDirectory) {
                        require(!target.exists() || target.isDirectory) { "Conflicting ZIP entry: $name" }
                        target.mkdirs()
                    } else {
                        require(!target.exists()) { "Conflicting ZIP entry: $name" }
                        target.write(false).use { output ->
                            while (true) {
                                val count = zip.read(buffer)
                                if (count < 0) break
                                total += count
                                require(total <= PackFormat.MAX_EXTRACTED_BYTES) { "Extracted pack exceeds 1 GiB" }
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
            require(names.size == entryCount) { "ZIP entry count mismatch" }
            require(names.contains("pack.json")) { "pack.json must be at the ZIP root" }
            return digest.digest().joinToString("") { (it.toInt() and 255).toString(16).padStart(2, '0') }
        } catch (exception: Exception) {
            destination.deleteDirectory()
            throw exception
        }
    }
}
