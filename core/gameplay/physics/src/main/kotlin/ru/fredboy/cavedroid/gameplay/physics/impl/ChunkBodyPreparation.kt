package ru.fredboy.cavedroid.gameplay.physics.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceMetric
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.performance.measure

/**
 * Main-thread owner of section revisions and load lifetimes. Only the snapshot builder runs on a
 * worker; installation and readiness callbacks run on the scope's main dispatcher.
 */
internal class ChunkBodyPreparation<Snapshot, Prepared>(
    performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
    private val scope: CoroutineScope,
    private val background: CoroutineDispatcher,
    private val sectionCount: Int,
    private val snapshot: (chunkX: Int, section: Int) -> Snapshot,
    private val createBuilder: () -> (Snapshot) -> Prepared,
    private val install: (chunkX: Int, section: Int, Prepared) -> Unit,
    private val onReady: (chunkX: Int) -> Unit,
) {

    private val performanceSession = performanceMonitor.currentSession()
    private val columns = mutableMapOf<Int, Column>()
    private val synchronousBuilder = createBuilder()

    fun load(chunkX: Int, synchronous: Boolean = false) {
        if (chunkX in columns) {
            if (synchronous) finish(chunkX)
            return
        }
        val column = Column(sectionCount)
        columns[chunkX] = column
        if (synchronous) {
            finish(chunkX)
            return
        }
        val span = performanceSession?.begin(PerformanceMetric.COLLISION_REQUEST)
        column.job = scope.launch(start = CoroutineStart.LAZY) {
            val build = createBuilder()
            for (section in 0 until sectionCount) {
                if (columns[chunkX] !== column) return@launch
                if (column.installed[section]) continue
                val revision = column.revisions[section]
                val input = snapshot(chunkX, section)
                val prepared = withContext(background) { performanceSession.measure(PerformanceMetric.COLLISION_BUILD) { build(input) } }
                if (columns[chunkX] !== column) return@launch
                if (column.revisions[section] != revision) continue
                performanceSession.measure(PerformanceMetric.COLLISION_APPLY) { install(chunkX, section, prepared) }
                column.installed[section] = true
            }
            notifyIfReady(chunkX, column)
        }.also { job ->
            job.invokeOnCompletion { cause ->
                if (cause != null) span?.fail(cause)
                span?.finish()
            }
            job.start()
        }
    }

    /** Complete a visible or teleported-to column before physics can step through missing bodies. */
    fun finish(chunkX: Int) {
        val column = columns[chunkX] ?: return
        if (column.ready) return
        column.job?.cancel()
        for (section in 0 until sectionCount) {
            if (!column.installed[section]) rebuildSection(chunkX, section, column)
        }
        notifyIfReady(chunkX, column)
    }

    fun rebuild(chunkX: Int, section: Int) {
        val column = columns[chunkX] ?: return
        rebuildSection(chunkX, section, column)
        notifyIfReady(chunkX, column)
    }

    fun unload(chunkX: Int) {
        columns.remove(chunkX)?.job?.cancel()
    }

    fun clear() {
        columns.values.forEach { it.job?.cancel() }
        columns.clear()
    }

    private fun rebuildSection(chunkX: Int, section: Int, column: Column) {
        column.revisions[section]++
        val prepared = performanceSession.measure(PerformanceMetric.COLLISION_BUILD) { synchronousBuilder(snapshot(chunkX, section)) }
        performanceSession.measure(PerformanceMetric.COLLISION_APPLY) { install(chunkX, section, prepared) }
        column.installed[section] = true
    }

    private fun notifyIfReady(chunkX: Int, column: Column) {
        if (columns[chunkX] === column && !column.ready && column.installed.all { it }) {
            column.ready = true
            onReady(chunkX)
        }
    }

    // Object identity is the load epoch: an unload/reload can never accept the old job's results.
    private class Column(sectionCount: Int) {
        val revisions = IntArray(sectionCount)
        val installed = BooleanArray(sectionCount)
        var ready = false
        var job: Job? = null
    }
}
