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
import ru.fredboy.cavedroid.common.api.AssetPackPlatform
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AssetPackRepositoryTest {
    @TempDir lateinit var temporary: Path
    private val previousFiles = Gdx.files
    private val values = mutableMapOf<String, String>()
    private val preferences = object : PreferencesStore {
        override fun getPreference(key: String) = values[key]
        override fun setPreference(key: String, value: String?) {
            if (value == null) values.remove(key) else values[key] = value
        }
    }
    private val platform = object : AssetPackPlatform {
        override val isSupported = true
        override fun pickArchive(onResult: (Result<FileHandle?>) -> Unit) = Unit
        override fun extractArchive(archive: FileHandle, destination: FileHandle) = NativePackArchives.extract(archive, destination)
    }
    private lateinit var context: ApplicationContextRepository

    @BeforeEach
    fun setup() {
        context = mockk {
            every { getGameDirectory() } returns temporary.toString()
            every { getGameDirectoryFileType() } returns Files.FileType.Absolute
        }
        Gdx.files = mockk { every { getFileHandle(any(), any()) } answers { FileHandle(firstArg<String>()) } }
    }

    @AfterEach
    fun cleanup() {
        Gdx.files = previousFiles
    }

    private fun repository() = AssetPackRepositoryImpl(platform, preferences, context)

    private fun example(): FileHandle {
        val archive = FileHandle(temporary.resolve("input.zip").toFile())
        val bytes = javaClass.getResourceAsStream("/packs/checker-stone-32x.zip")!!.use { it.readBytes() }
        archive.writeBytes(bytes, false)
        return archive
    }

    @Test
    fun `documented example installs disabled and duplicate import is idempotent`() {
        val repository = repository()
        val first = repository.importArchive(example())
        assertEquals("Checker Stone 32x", first.metadata.name)
        assertEquals(first.id, repository.importArchive(example()).id)
        assertEquals(1, repository.installed().size)
        assertTrue(repository.pendingIds.isEmpty())
        assertFalse(temporary.resolve("input.zip").toFile().exists())
    }

    @Test
    fun `pending selection becomes running in a new asset generation`() {
        val repository = repository()
        val pack = repository.importArchive(example())
        repository.saveSelection(listOf(pack.id))
        assertTrue(repository.runningIds.isEmpty())
        assertEquals(listOf(pack.id), repository.pendingIds)
        assertEquals(listOf(pack.id), repository().runningIds)
    }

    @Test
    fun `running pack removal preserves files until its asset generation is released`() {
        val first = repository()
        val pack = first.importArchive(example())
        first.saveSelection(listOf(pack.id))
        val running = repository()
        running.remove(pack.id)
        assertTrue(pack.directory.child("textures/textures/blocks/stone.png").exists())
        assertTrue(running.installed().isEmpty())
        assertTrue(running.pendingIds.isEmpty())
        val next = repository()
        assertTrue(pack.directory.exists())
        next.cleanupRemoved()
        assertFalse(pack.directory.exists())
    }

    @Test
    fun `failure recovery clears next stack but retains installed packs and diagnostic`() {
        val repository = repository()
        val pack = repository.importArchive(example())
        repository.saveSelection(listOf(pack.id))
        repository.disableAfterFailure("Invalid merged mob")
        val recovered = repository()
        assertTrue(recovered.runningIds.isEmpty())
        assertEquals(1, recovered.installed().size)
        assertEquals("Invalid merged mob", recovered.startupError)
    }

    @Test
    fun `invalid import leaves no installed or staging files`() {
        val archive = FileHandle(temporary.resolve("invalid.zip").toFile())
        ZipOutputStream(archive.write(false)).use { zip ->
            zip.putNextEntry(ZipEntry("pack.json"))
            zip.write("{}".toByteArray())
            zip.closeEntry()
        }
        val repository = repository()
        assertThrows(Exception::class.java) { repository.importArchive(archive) }
        assertTrue(repository.installed().isEmpty())
        assertEquals(0, temporary.resolve("asset-packs").toFile().list()!!.size)
        assertFalse(archive.exists())
    }

    @Test
    fun `documentation metadata parses and example paths exist in bundled assets`() {
        val root = generateSequence(Path.of("").toAbsolutePath()) { it.parent }.first { it.resolve("ASSET_PACKS.md").toFile().exists() }
        val guide = root.resolve("ASSET_PACKS.md").toFile().readText()
        val json = Regex("```json\\s*(.*?)```", RegexOption.DOT_MATCHES_ALL).find(guide)!!.groupValues[1]
        assertEquals("My Stone Pack", PackFormat.metadata(json).name)
        assertTrue(root.resolve("assets/textures/textures/blocks/stone.png").toFile().exists())
    }
}
