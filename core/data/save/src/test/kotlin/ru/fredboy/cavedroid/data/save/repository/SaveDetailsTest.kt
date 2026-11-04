package ru.fredboy.cavedroid.data.save.repository

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.model.WorldType
import ru.fredboy.cavedroid.common.utils.DateFormatter
import ru.fredboy.cavedroid.data.save.model.SaveDataDto
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
class SaveDetailsTest {
    @TempDir
    lateinit var gameDirectory: File

    private var previousFiles: Files? = null
    private lateinit var repository: SaveDataRepositoryImpl

    @BeforeEach
    fun setUp() {
        previousFiles = Gdx.files
        Gdx.files = mockk<Files> {
            every { getFileHandle(any(), Files.FileType.Absolute) } answers { FileHandle(File(firstArg<String>())) }
        }
        val applicationContextRepository = mockk<ApplicationContextRepository> {
            every { getGameDirectoryFileType() } returns Files.FileType.Absolute
        }
        repository = SaveDataRepositoryImpl(
            itemsRepository = mockk(),
            dropControllerMapper = mockk(),
            containerControllerMapper = mockk(),
            mobControllerMapper = mockk(),
            gameSaveInfoMapper = mockk(),
            projectileControllerMapper = mockk(),
            growBlocksMapper = mockk(),
            fireControllerMapper = mockk(),
            tntMapper = mockk(),
            getItemByKeyUseCase = mockk(),
            applicationContextRepository = applicationContextRepository,
            dateFormatter = DateFormatter(),
        )
    }

    @AfterEach
    fun tearDown() {
        Gdx.files = previousFiles
    }

    @Test
    fun `infinite save size includes all chunk files and ignores empty directories`() {
        val save = createSave(WorldType.INFINITE)
        File(save, "mobs.dat").writeBytes(ByteArray(11))
        val chunk = File(save, "chunks/-1").apply { mkdirs() }
        File(chunk, "foremap.dat.gz").writeBytes(ByteArray(101))
        File(chunk, "entities.dat.gz").writeBytes(ByteArray(37))
        val otherChunk = File(save, "chunks/2").apply { mkdirs() }
        File(otherChunk, "backmap.dat.gz").writeBytes(ByteArray(53))
        File(save, "chunks/empty").mkdirs()
        File(gameDirectory, "unrelated.dat").writeBytes(ByteArray(999))

        val details = repository.getSaveDetails(gameDirectory.path, "world")

        assertEquals(File(save, "meta.dat").length() + 11L + 101L + 37L + 53L, details.sizeBytes)
    }

    @Test
    fun `finite save size still includes top level files`() {
        val save = createSave(WorldType.LOOPING)
        val foremap = File(save, "foremap.dat.gz")
        foremap.writeBytes(ByteArray(41))
        File(save, "backmap.dat.gz").writeBytes(ByteArray(73))

        val details = repository.getSaveDetails(gameDirectory.path, "world")

        assertEquals(File(save, "meta.dat").length() + foremap.length() + 73L, details.sizeBytes)
    }

    private fun createSave(worldType: WorldType): File {
        val save = File(gameDirectory, "saves/world").apply { mkdirs() }
        val meta = SaveDataDto.WorldSaveDataDto(
            version = 7,
            name = "world",
            timestamp = 0L,
            gameTime = 0f,
            moonPhase = 0,
            gameMode = GameMode.SURVIVAL,
            worldType = worldType.ordinal,
        )
        File(save, "meta.dat").writeBytes(ProtoBuf.encodeToByteArray(meta))
        return save
    }
}
