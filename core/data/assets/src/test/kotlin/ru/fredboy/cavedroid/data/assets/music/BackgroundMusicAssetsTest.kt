package ru.fredboy.cavedroid.data.assets.music

import com.badlogic.gdx.files.FileHandle
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.fredboy.cavedroid.domain.assets.pack.AssetResolver
import java.nio.file.Path

class BackgroundMusicAssetsTest {
    @TempDir lateinit var temporary: Path

    private fun assets(index: String): BackgroundMusicAssets {
        val root = FileHandle(temporary.toFile())
        val indexFile = root.child("bgm/index.json")
        indexFile.writeString(index, false)
        val resolver = mockk<AssetResolver> {
            every { handle(BackgroundMusicAssets.INDEX_PATH) } returns indexFile
            every { resolve(any()) } answers { root.child(firstArg()) }
        }
        return BackgroundMusicAssets(resolver)
    }

    private fun track(path: String) {
        FileHandle(temporary.toFile()).child("bgm/$path").writeBytes(byteArrayOf(1), false)
    }

    @Test
    fun `valid playlist loads track durations and permits an empty playlist`() {
        track("ambient.wav")
        val playlist = assets("""{"ambient.wav":12.5}""")
        playlist.validate()
        assertEquals(mapOf("ambient.wav" to 12.5f), playlist.trackDurations)
        assertTrue(assets("{}").trackDurations.isEmpty())
    }

    @Test
    fun `non object indexes and invalid duration types are rejected`() {
        track("ambient.wav")
        listOf("[]", "null", "1", """{"ambient.wav":"12"}""", """{"ambient.wav":null}""", """{"ambient.wav":{}}""")
            .forEach { index ->
                assertThrows(IllegalArgumentException::class.java) { assets(index).validate() }
            }
    }

    @Test
    fun `nonpositive and infinite durations are rejected`() {
        track("ambient.wav")
        listOf("0", "-1", "1e100", "1e-100").forEach { duration ->
            assertThrows(IllegalArgumentException::class.java) {
                assets("""{"ambient.wav":$duration}""").validate()
            }
        }
    }

    @Test
    fun `unsafe paths and duplicate tracks are rejected`() {
        track("ambient.wav")
        listOf("../outside.wav", "/absolute.wav", "", "nested/../ambient.wav").forEach { path ->
            assertThrows(IllegalArgumentException::class.java) {
                assets("""{"$path":60}""").validate()
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            assets("""{"ambient.wav":60,"ambient.wav":30}""").validate()
        }
    }

    @Test
    fun `missing files directories and empty files are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { assets("""{"missing.wav":60}""").validate() }
        val directory = FileHandle(temporary.resolve("bgm/directory.wav").toFile())
        directory.mkdirs()
        assertThrows(IllegalArgumentException::class.java) { assets("""{"directory.wav":60}""").validate() }
        FileHandle(temporary.resolve("bgm/empty.wav").toFile()).writeBytes(byteArrayOf(), false)
        assertThrows(IllegalArgumentException::class.java) { assets("""{"empty.wav":60}""").validate() }
    }
}
