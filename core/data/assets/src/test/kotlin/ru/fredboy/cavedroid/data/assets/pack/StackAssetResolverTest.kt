package ru.fredboy.cavedroid.data.assets.pack

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackRepository
import ru.fredboy.cavedroid.domain.assets.pack.InstalledAssetPack
import ru.fredboy.cavedroid.domain.assets.pack.PackMetadata
import java.nio.file.Path

class StackAssetResolverTest {
    @TempDir lateinit var temporary: Path
    private val previousFiles = Gdx.files
    private lateinit var bundled: FileHandle

    @BeforeEach
    fun setup() {
        bundled = FileHandle(temporary.resolve("bundled").toFile())
        Gdx.files = mockk<Files> { every { internal(any()) } answers { bundled.child(firstArg()) } }
    }

    @AfterEach
    fun cleanup() {
        Gdx.files = previousFiles
    }

    private fun pack(id: String, scales: Map<String, Int> = emptyMap()) = InstalledAssetPack(
        id,
        PackMetadata(1, id, "1", "Author", "CC0", "icon.png", scales),
        FileHandle(temporary.resolve(id).toFile()),
    )

    private fun resolver(vararg packs: InstalledAssetPack): StackAssetResolver {
        val repository = mockk<AssetPackRepository> {
            every { runningIds } returns packs.map { it.id }
            every { installed() } returns packs.toList()
        }
        return StackAssetResolver(repository)
    }

    @Test
    fun `higher pack wins while missing files fall through each layer`() {
        val high = pack("high")
        val low = pack("low")
        bundled.child("a.txt").writeString("bundled", false)
        bundled.child("b.txt").writeString("fallback", false)
        low.directory.child("a.txt").writeString("low", false)
        high.directory.child("c.txt").writeString("high", false)
        val resolver = resolver(high, low)
        assertEquals("low", resolver.handle("a.txt").readString())
        assertEquals("fallback", resolver.handle("b.txt").readString())
        assertEquals("high", resolver.handle("c.txt").readString())
        assertEquals(low.directory.child("a.txt").path(), resolver.resolve("a.txt").path())
    }

    @Test
    fun `nested references retain stack and JSON merges from bottom to top`() {
        val high = pack("high")
        val low = pack("low")
        bundled.child("json/mobs.json").writeString("""{"pig":{"hp":5,"speed":1},"cow":{}}""", false)
        low.directory.child("json/mobs.json").writeString("""{"pig":{"hp":10}}""", false)
        high.directory.child("json/mobs.json").writeString("""{"pig":{"hp":20}}""", false)
        bundled.child("skin/font/page.png").writeString("bundled", false)
        low.directory.child("skin/font/page.png").writeString("page", false)
        val resolver = resolver(high, low)
        assertEquals("""{"pig":{"hp":20,"speed":1},"cow":{}}""", resolver.handle("json/mobs.json").readString())
        assertEquals("page", resolver.handle("skin/font/font.fnt").parent().child("page.png").readString())
        assertFalse(resolver.handle("json/missing.json").exists())
    }

    @Test
    fun `texture scale belongs to image provider and bundled fallback stays unscaled`() {
        val high = pack("high", mapOf("new.png" to 4))
        val low = pack("low", mapOf("new.png" to 2))
        low.directory.child("new.png").writeString("pixels", false)
        val resolver = resolver(high, low)
        assertEquals(2, resolver.textureScale("new.png", 32, 32))
        assertEquals(1, resolver.textureScale("bundled.png", 16, 16))
    }

    @Test
    fun `missing enabled pack fails rather than silently changing the stack`() {
        val repository = mockk<AssetPackRepository> {
            every { runningIds } returns listOf("missing")
            every { installed() } returns emptyList()
        }
        assertThrows(IllegalStateException::class.java) { StackAssetResolver(repository).resolve("file.txt") }
    }

    @Test
    fun `highest priority music index replaces lower playlists and built in tracks`() {
        val high = pack("high")
        val low = pack("low")
        bundled.child("bgm/index.json").writeString("""{"0.wav":60,"1.wav":120}""", false)
        low.directory.child("bgm/index.json").writeString("""{"low.wav":30}""", false)
        val playlist = """{"high.wav":45}"""
        high.directory.child("bgm/index.json").writeString(playlist, false)
        val index = resolver(high, low).handle("bgm/index.json")
        assertEquals(playlist, index.readString())
        assertEquals(playlist.toByteArray().size.toLong(), index.length())
    }

    @Test
    fun `pack without music index falls back while an empty index disables music`() {
        val high = pack("high")
        val low = pack("low")
        val playlist = """{"0.wav":60}"""
        bundled.child("bgm/index.json").writeString(playlist, false)
        assertEquals(playlist, resolver(high, low).handle("bgm/index.json").readString())
        low.directory.child("bgm/index.json").writeString("{}", false)
        assertEquals("{}", resolver(high, low).handle("bgm/index.json").readString())
    }
}
