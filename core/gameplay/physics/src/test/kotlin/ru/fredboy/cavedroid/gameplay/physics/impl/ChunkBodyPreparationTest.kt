package ru.fredboy.cavedroid.gameplay.physics.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class ChunkBodyPreparationTest {

    @Test
    fun `edits reject an already prepared stale section`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(-1)
        runCurrent()
        fixture.worker.runCurrent()
        fixture.terrain = 2
        fixture.preparation.rebuild(-1, 0)
        fixture.drain()
        assertEquals(listOf(2, 2), fixture.installed.values.toList())
        assertEquals(2, fixture.installCount)
        assertEquals(listOf(-1), fixture.ready)
    }

    @Test
    fun `unload during preparation cannot install bodies or notify readiness`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(1)
        runCurrent()
        fixture.worker.runCurrent()
        fixture.preparation.unload(1)
        fixture.drain()
        assertTrue(fixture.installed.isEmpty())
        assertTrue(fixture.ready.isEmpty())
    }

    @Test
    fun `reloaded column cannot accept or be destroyed by its previous load`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(1)
        runCurrent()
        fixture.worker.runCurrent()
        fixture.preparation.unload(1)
        fixture.terrain = 3
        fixture.preparation.load(1)
        fixture.drain()
        assertEquals(listOf(3, 3), fixture.installed.values.toList())
        assertEquals(listOf(1), fixture.ready)
        assertEquals(2, fixture.installCount)
    }

    @Test
    fun `duplicate loads and later edits do not repeat readiness`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(1)
        fixture.preparation.load(1)
        fixture.drain()
        fixture.preparation.load(1)
        fixture.preparation.rebuild(1, 1)
        fixture.drain()
        assertEquals(listOf(1), fixture.ready)
        assertEquals(3, fixture.installCount)
    }

    @Test
    fun `visible column completes synchronously before workers finish`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(1)
        runCurrent()
        fixture.terrain = 4
        fixture.preparation.finish(1)
        assertEquals(listOf(4, 4), fixture.installed.values.toList())
        assertEquals(listOf(1), fixture.ready)
        fixture.drain()
        assertEquals(2, fixture.installCount)
    }

    @Test
    fun `editing all pending sections can make the column ready once`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(1)
        runCurrent()
        fixture.preparation.rebuild(1, 0)
        fixture.preparation.rebuild(1, 1)
        assertEquals(listOf(1), fixture.ready)
        fixture.drain()
        assertEquals(listOf(1), fixture.ready)
        assertEquals(2, fixture.installCount)
    }

    @Test
    fun `disposal discards worker completions`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(1)
        runCurrent()
        fixture.worker.runCurrent()
        fixture.preparation.clear()
        fixture.drain()
        assertTrue(fixture.installed.isEmpty())
        assertTrue(fixture.ready.isEmpty())
    }

    @Test
    fun `initial synchronous loading requires no dispatcher progress`() = runTest {
        val fixture = Fixture(this)
        fixture.preparation.load(-2, synchronous = true)
        assertEquals(2, fixture.installed.size)
        assertEquals(listOf(-2), fixture.ready)
        fixture.drain()
        assertEquals(2, fixture.installCount)
    }

    private class Fixture(private val scope: TestScope) {
        val worker = QueuedDispatcher()
        var terrain = 1
        val installed = linkedMapOf<Pair<Int, Int>, Int>()
        val ready = mutableListOf<Int>()
        var installCount = 0
        val preparation = ChunkBodyPreparation(
            scope = scope,
            background = worker,
            sectionCount = 2,
            snapshot = { _: Int, _: Int -> terrain },
            createBuilder = { { input: Int -> input } },
            install = { chunkX, section, result: Int ->
                installed[chunkX to section] = result
                installCount++
            },
            onReady = { ready.add(it) },
        )

        fun drain() {
            repeat(5) {
                scope.testScheduler.runCurrent()
                worker.runCurrent()
            }
            scope.testScheduler.runCurrent()
        }
    }

    /** Pause worker completion independently of the main test scheduler. */
    private class QueuedDispatcher : CoroutineDispatcher() {
        private val queue = ArrayDeque<Runnable>()

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            queue.addLast(block)
        }

        fun runCurrent() {
            while (queue.isNotEmpty()) queue.removeFirst().run()
        }
    }
}
