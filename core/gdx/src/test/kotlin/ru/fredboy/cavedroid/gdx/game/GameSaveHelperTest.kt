package ru.fredboy.cavedroid.gdx.game

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.data.save.streaming.ChunkEntityStreamer
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.game.controller.stats.StatsController
import ru.fredboy.cavedroid.game.world.GameWorld

class GameSaveHelperTest {
    private val repository = mockk<SaveDataRepository>(relaxed = true)
    private val world = mockk<GameWorld>(relaxed = true) {
        every { isInfinite } returns true
    }
    private val streamer = mockk<ChunkEntityStreamer>(relaxed = true)
    private val stats = mockk<StatsController>(relaxed = true)
    private val helper = GameSaveHelper(
        applicationContextRepository = mockk(relaxed = true),
        gameContextRepository = mockk(relaxed = true),
        dropController = mockk(relaxed = true),
        mobController = mockk(relaxed = true),
        gameWorld = world,
        containerController = mockk(relaxed = true),
        saveDataRepository = repository,
        projectileController = mockk(relaxed = true),
        statsController = stats,
        growBlocksControllerTask = mockk(relaxed = true),
        fireController = mockk(relaxed = true),
        explosionController = mockk(relaxed = true),
        chunkEntityStreamer = streamer,
        vehicles = mockk(relaxed = true),
        performanceMonitor = NoOpPerformanceMonitor,
    )

    @Test
    fun `full save runs every sixty seconds including resident entities and player payload`() {
        helper.updateAutoSave(59f)
        verify(exactly = 0) { repository.save(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }

        helper.updateAutoSave(1f)
        verify(exactly = 1) { streamer.flushAll() }
        verify(exactly = 1) { repository.save(any(), any(), any(), any(), any(), any(), world, any(), any(), any(), any(), any()) }
        verify(exactly = 1) { stats.onSaveCheckpoint() }

        helper.updateAutoSave(60f)
        verify(exactly = 2) { stats.onSaveCheckpoint() }
        verify(exactly = 2) { repository.getActualSaveDirName(any(), any(), overwrite = true) }
    }

    @Test
    fun `manual save restarts the autosave interval`() {
        helper.updateAutoSave(59f)
        helper.saveGame(overwrite = true)
        helper.updateAutoSave(1f)
        verify(exactly = 1) { stats.onSaveCheckpoint() }

        helper.updateAutoSave(59f)
        verify(exactly = 2) { stats.onSaveCheckpoint() }
    }

    @Test
    fun `long frames trigger one save without catchup saves`() {
        helper.updateAutoSave(180f)
        helper.updateAutoSave(0.1f)
        verify(exactly = 1) { stats.onSaveCheckpoint() }
    }

    @Test
    fun `failed autosave retries after an interval without interrupting gameplay`() {
        every { repository.getActualSaveDirName(any(), any(), any()) } throws IllegalStateException("Disk unavailable")
        helper.updateAutoSave(60f)
        helper.updateAutoSave(1f)
        verify(exactly = 1) { repository.getActualSaveDirName(any(), any(), any()) }
        verify(exactly = 0) { stats.onSaveCheckpoint() }

        every { repository.getActualSaveDirName(any(), any(), any()) } returns "world"
        helper.updateAutoSave(59f)
        verify(exactly = 1) { stats.onSaveCheckpoint() }
    }
}
