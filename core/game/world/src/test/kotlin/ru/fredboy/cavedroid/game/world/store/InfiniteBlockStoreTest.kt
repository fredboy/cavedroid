package ru.fredboy.cavedroid.game.world.store

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

@OptIn(ExperimentalCoroutinesApi::class)
class InfiniteBlockStoreTest {

    @Test
    fun `workers publish terrain and callbacks only after returning to main`() = runTest {
        val fixture = Fixture(this)
        fixture.move(1)
        runCurrent()
        fixture.worker.runCurrent()
        assertFalse(fixture.store.isChunkLoaded(7 * 16))
        assertTrue(fixture.loaded.isEmpty())
        runCurrent()
        assertTrue(fixture.store.isChunkLoaded(7 * 16))
        assertEquals(listOf(7), fixture.loaded)
        fixture.close()
    }

    @Test
    fun `loaded chunk enumeration excludes outstanding requests`() = runTest {
        val fixture = Fixture(this)
        fixture.move(1)
        runCurrent()
        val loaded = mutableListOf<Int>()
        fixture.store.forEachLoadedChunk { loaded.add(it) }
        assertEquals((-6..6).toSet(), loaded.toSet())
        fixture.close()
    }

    @Test
    fun `moving away rejects an old request even after its worker finished`() = runTest {
        val fixture = Fixture(this)
        fixture.move(1)
        runCurrent()
        fixture.worker.runCurrent()
        fixture.move(-1)
        fixture.drain()
        assertFalse(fixture.store.isChunkLoaded(7 * 16))
        assertEquals(listOf(-7), fixture.loaded)
        fixture.close()
    }

    @Test
    fun `teleport publishes destination terrain synchronously without duplicate loads`() = runTest {
        val fixture = Fixture(this)
        fixture.move(1)
        runCurrent()
        fixture.worker.runCurrent()
        fixture.move(7)
        assertTrue(fixture.store.isChunkLoaded(7 * 16))
        fixture.drain()
        assertEquals(1, fixture.loaded.count { it == 7 })
        fixture.close()
    }

    @Test
    fun `queued eviction save uses a detached snapshot on immediate reload`() = runTest {
        val fixture = Fixture(this)
        val first = mockk<Block>()
        val second = mockk<Block>()
        fixture.store.setBlock(-96, 0, Layer.FOREGROUND, first)
        fixture.move(3)
        assertTrue(-6 in fixture.unloaded)
        fixture.move(-6)
        assertSame(first, fixture.store.getBlock(-96, 0, Layer.FOREGROUND))
        fixture.store.setBlock(-96, 0, Layer.FOREGROUND, second)
        fixture.io.runCurrent()
        assertSame(first, fixture.saved.last().fore[0][0])
        fixture.store.flushDirtyChunks()
        assertSame(second, fixture.saved.last().fore[0][0])
        fixture.close()
    }

    @Test
    fun `late eviction write cannot overwrite a newer explicit save`() = runTest {
        val fixture = Fixture(this)
        val first = mockk<Block>()
        val second = mockk<Block>()
        fixture.store.setBlock(-96, 0, Layer.FOREGROUND, first)
        fixture.move(3)
        fixture.move(-6)
        fixture.store.setBlock(-96, 0, Layer.FOREGROUND, second)
        fixture.store.flushDirtyChunks()
        fixture.drain()
        assertEquals(1, fixture.saved.count { it.chunkX == -6 })
        assertSame(second, fixture.saved.single { it.chunkX == -6 }.fore[0][0])
        fixture.close()
    }

    @Test
    fun `disposal flushes evicted chunks and rejects pending load callbacks`() = runTest {
        val fixture = Fixture(this)
        val edited = mockk<Block>()
        fixture.store.setBlock(-96, 0, Layer.FOREGROUND, edited)
        fixture.move(3)
        runCurrent()
        fixture.worker.runCurrent()
        fixture.close()
        assertSame(edited, fixture.saved.single { it.chunkX == -6 }.fore[0][0])
        assertTrue(fixture.loaded.isEmpty())
    }

    @Test
    fun `failed persistence retains dirty terrain for retry`() = runTest {
        val fixture = Fixture(this)
        fixture.store.setBlock(0, 0, Layer.FOREGROUND, mockk())
        fixture.failSave = true
        try {
            fixture.store.flushDirtyChunks()
        } catch (_: IllegalStateException) {
            // The next explicit save must still see the dirty resident chunk.
        }
        fixture.failSave = false
        fixture.store.flushDirtyChunks()
        assertEquals(1, fixture.saved.count { it.chunkX == 0 })
        fixture.close()
    }

    private class Fixture(private val scope: TestScope) {
        val worker = TestCoroutineScheduler()
        val io = TestCoroutineScheduler()
        val air = mockk<Block.None>()
        val visible = Rectangle(0f, 0f, 0f, 16f)
        val saved = mutableListOf<Chunk>()
        val loaded = mutableListOf<Int>()
        val unloaded = mutableListOf<Int>()
        var failSave = false
        private val repository = mockk<ItemsRepository> { every { fallbackBlock } returns air }
        private val context = mockk<GameContextRepository> {
            every { getCameraContext() } returns CameraContext(Rectangle(), visible)
        }
        val store = InfiniteBlockStore(
            itemsRepository = repository,
            generatorConfig = WorldGeneratorConfig.getDefault(),
            gameContextRepository = context,
            appDispatchers = AppDispatchers(
                io = StandardTestDispatcher(io),
                background = StandardTestDispatcher(worker),
                main = StandardTestDispatcher(scope.testScheduler),
            ),
            chunkLoader = { x ->
                Chunk(x, Array(16) { Array<Block>(256) { air } }, Array(16) { Array<Block>(256) { air } }, Array(16) { Biome.PLAINS })
            },
            chunkPersister = {
                check(!failSave)
                saved.add(it)
            },
        )

        init {
            store.addChunkListener(object : ChunkListener {
                override fun onChunkLoaded(chunkX: Int) {
                    assertTrue(store.isChunkLoaded(chunkX * 16))
                    loaded.add(chunkX)
                }

                override fun onChunkUnloaded(chunkX: Int) {
                    assertFalse(store.isChunkLoaded(chunkX * 16))
                    unloaded.add(chunkX)
                }
            })
        }

        fun move(chunkX: Int) {
            visible.x = chunkX * 16f
            store.update()
        }

        fun drain() {
            repeat(3) {
                scope.testScheduler.runCurrent()
                worker.runCurrent()
                io.runCurrent()
            }
            scope.testScheduler.runCurrent()
        }

        fun close() {
            store.dispose()
            drain()
        }
    }
}
