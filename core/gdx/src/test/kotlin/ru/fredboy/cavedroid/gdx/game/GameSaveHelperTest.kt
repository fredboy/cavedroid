package ru.fredboy.cavedroid.gdx.game

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.data.save.streaming.ChunkEntityStreamer
import ru.fredboy.cavedroid.domain.save.PreparedSave
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.game.controller.stats.StatsController
import ru.fredboy.cavedroid.game.world.GameWorld
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
class GameSaveHelperTest {
    private val repository = mockk<SaveDataRepository>(relaxed = true)
    private val world = mockk<GameWorld>(relaxed = true) {
        every { isInfinite } returns true
    }
    private val streamer = mockk<ChunkEntityStreamer>(relaxed = true)
    private val stats = mockk<StatsController>(relaxed = true)
    private val prepared = mockk<PreparedSave>(relaxed = true)

    private fun helper(io: CoroutineDispatcher, main: CoroutineDispatcher): GameSaveHelper {
        every { repository.prepareSave(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns prepared
        return GameSaveHelper(
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
            dispatchers = AppDispatchers(io, io, main),
        )
    }

    @Test
    fun `autosave captures every sixty active seconds and finalizes on render thread`() = runTest {
        val helper = helper(StandardTestDispatcher(testScheduler), StandardTestDispatcher(testScheduler))
        helper.updateAutoSave(59f)
        verify(exactly = 0) { prepared.write() }
        helper.updateAutoSave(1f)
        verify(exactly = 1) { streamer.prepareAll() }
        verify(exactly = 0) { prepared.write() }
        runCurrent()
        verify(exactly = 1) { prepared.write() }
        verify(exactly = 0) { stats.onSaveCheckpoint() }
        helper.updateAutoSave(0.1f)
        verify(exactly = 1) {
            prepared.finish()
            stats.onSaveCheckpoint()
        }
        helper.updateAutoSave(59f)
        verify(exactly = 1) { streamer.prepareAll() }
        helper.updateAutoSave(1f)
        runCurrent()
        helper.dispose()
        verify(exactly = 2) { stats.onSaveCheckpoint() }
    }

    @Test
    fun `only one save runs and long frames do not queue catchup saves`() = runTest {
        val helper = helper(StandardTestDispatcher(testScheduler), StandardTestDispatcher(testScheduler))
        helper.updateAutoSave(180f)
        helper.updateAutoSave(180f)
        verify(exactly = 1) { streamer.prepareAll() }
        runCurrent()
        helper.updateAutoSave(180f)
        helper.updateAutoSave(0.1f)
        verify(exactly = 1) {
            prepared.write()
            stats.onSaveCheckpoint()
        }
        helper.dispose()
    }

    @Test
    fun `failed persistence is finalized and retries after another interval`() = runTest {
        val helper = helper(StandardTestDispatcher(testScheduler), StandardTestDispatcher(testScheduler))
        every { prepared.write() } throws IllegalStateException("Disk unavailable")
        helper.updateAutoSave(60f)
        runCurrent()
        helper.updateAutoSave(60f)
        verify(exactly = 1) { prepared.finish() }
        verify(exactly = 0) { stats.onSaveCheckpoint() }
        every { prepared.write() } returns Unit
        helper.updateAutoSave(59f)
        verify(exactly = 1) { prepared.write() }
        helper.updateAutoSave(1f)
        runCurrent()
        helper.dispose()
        verify(exactly = 1) { stats.onSaveCheckpoint() }
    }

    @Test
    fun `preparation failure restarts interval`() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val helper = helper(main, main)
        every { repository.getActualSaveDirName(any(), any(), any()) } throws IllegalStateException("Unavailable")
        helper.updateAutoSave(60f)
        helper.updateAutoSave(59f)
        verify(exactly = 1) { repository.getActualSaveDirName(any(), any(), any()) }
        every { repository.getActualSaveDirName(any(), any(), any()) } returns "world"
        helper.updateAutoSave(1f)
        verify(exactly = 1) { stats.onSaveCheckpoint() }
        helper.dispose()
    }

    @Test
    fun `shared web dispatcher persists synchronously without a queued task`() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val helper = helper(main, main)
        helper.updateAutoSave(60f)
        verifyOrder {
            prepared.write()
            prepared.finish()
            stats.onSaveCheckpoint()
        }
        runCurrent()
        verify(exactly = 1) { prepared.write() }
        helper.dispose()
    }

    @Test
    @Timeout(10)
    fun `manual save drains worker without render callbacks and captures fresh state`() = runTest {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher().use { io ->
            val helper = helper(io, StandardTestDispatcher(testScheduler))
            val renderThread = Thread.currentThread()
            every { prepared.write() } answers { assertNotEquals(renderThread, Thread.currentThread()) }
            helper.updateAutoSave(60f)
            helper.drain()
            every { prepared.write() } returns Unit
            helper.saveGame(overwrite = true)
            verifyOrder {
                prepared.write()
                prepared.finish()
                stats.onSaveCheckpoint()
                streamer.prepareAll()
                prepared.write()
                prepared.finish()
                stats.onSaveCheckpoint()
            }
            helper.updateAutoSave(59f)
            verify(exactly = 2) { prepared.write() }
            helper.dispose()
        }
    }

    @Test
    @Timeout(10)
    fun `drain logs autosave failure and manual save still proceeds`() = runTest {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher().use { io ->
            val helper = helper(io, StandardTestDispatcher(testScheduler))
            every { prepared.write() } throws IllegalStateException("Disk unavailable") andThen Unit
            helper.updateAutoSave(60f)
            helper.saveGame(overwrite = true)
            verify(exactly = 2) {
                prepared.write()
                prepared.finish()
            }
            verify(exactly = 1) { stats.onSaveCheckpoint() }
            helper.dispose()
        }
    }

    @Test
    @Timeout(10)
    fun `pause drain and disposal finish persistence exactly once and do not initiate saves`() = runTest {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher().use { io ->
            val helper = helper(io, StandardTestDispatcher(testScheduler))
            helper.drain()
            helper.updateAutoSave(60f)
            helper.dispose()
            helper.drain()
            helper.dispose()
            helper.updateAutoSave(60f)
            verify(exactly = 1) {
                prepared.write()
                prepared.finish()
                stats.onSaveCheckpoint()
            }
        }
    }
}
