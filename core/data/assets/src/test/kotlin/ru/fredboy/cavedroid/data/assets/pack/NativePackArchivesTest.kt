package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.files.FileHandle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class NativePackArchivesTest {
    @TempDir lateinit var temporary: Path

    private fun zip(vararg entries: Pair<String, String>): FileHandle {
        val file = FileHandle(temporary.resolve("pack.zip").toFile())
        ZipOutputStream(file.write(false)).use { zip ->
            entries.forEach { (name, text) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(text.toByteArray())
                zip.closeEntry()
            }
        }
        return file
    }

    @Test
    fun `extracts real deflated ZIP and returns stable content identity`() {
        val archive = zip("pack.json" to "{}", "textures/stone.png" to "image")
        val first = FileHandle(temporary.resolve("first").toFile())
        val second = FileHandle(temporary.resolve("second").toFile())
        val hash = NativePackArchives.extract(archive, first)
        assertEquals(64, hash.length)
        assertEquals(hash, NativePackArchives.extract(archive, second))
        assertEquals("image", first.child("textures/stone.png").readString())
    }

    @Test
    fun `rejects traversal and removes partially extracted files`() {
        val archive = zip("pack.json" to "{}", "../outside" to "bad")
        val destination = FileHandle(temporary.resolve("pack").toFile())
        assertThrows(IllegalArgumentException::class.java) { NativePackArchives.extract(archive, destination) }
        assertFalse(destination.exists())
        assertFalse(temporary.resolve("outside").toFile().exists())
    }

    @Test
    fun `rejects archive with enclosing assets directory`() {
        val archive = zip("assets/pack.json" to "{}")
        val destination = FileHandle(temporary.resolve("pack").toFile())
        assertThrows(IllegalArgumentException::class.java) { NativePackArchives.extract(archive, destination) }
        assertFalse(destination.exists())
    }

    @Test
    fun `rejects colliding file and directory entries`() {
        val archive = zip("pack.json" to "{}", "textures/file" to "x", "textures" to "y")
        assertThrows(IllegalArgumentException::class.java) {
            NativePackArchives.extract(archive, FileHandle(temporary.resolve("pack").toFile()))
        }
    }

    @Test
    fun `rejects truncated compressed data`() {
        val archive = zip("pack.json" to "{}", "large.txt" to "example".repeat(1000))
        archive.writeBytes(archive.readBytes().copyOf(45), false)
        assertThrows(Exception::class.java) {
            NativePackArchives.extract(archive, FileHandle(temporary.resolve("pack").toFile()))
        }
    }
}
