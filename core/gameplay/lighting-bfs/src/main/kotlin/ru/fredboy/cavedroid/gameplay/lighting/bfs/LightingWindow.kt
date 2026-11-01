package ru.fredboy.cavedroid.gameplay.lighting.bfs

import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.store.ChunkListener
import kotlin.math.abs

/** Resumable strips publish independently; each includes a halo covering every light influence. */
internal class LightingWindow(
    private val height: Int,
    private val isOpaque: (Int, Int) -> Boolean,
    private val blockEmission: (Int, Int) -> Int,
) : ChunkListener {
    private var originX = -WIDTH / 2
    private val pending = mutableMapOf<Int, Strip>()
    private val readyColumns = mutableSetOf<Int>()
    private data class Emitter(val x: Int, val y: Int, val level: Int)
    private val emitters = mutableMapOf<Long, Emitter>()

    var grid = LightGrid(WIDTH, height, originX, looping = false)
        private set

    init {
        scheduleMissing()
        update(0)
    }

    private inner class Strip(val chunk: Int) {
        val start = chunk * STRIP_WIDTH
        val origin = start - HALO
        val candidate = LightGrid(STRIP_WIDTH + 2 * HALO, height, origin, looping = false).apply {
            copyTransientEmittersFrom(grid)
            for ((id, emitter) in emitters) seedTransientEmitter(id, emitter.x, emitter.y, emitter.level)
        }
        private val rebuild = candidate.beginRebuild(
            isOpaque = { x, y -> isOpaque(x + origin, y) },
            blockEmission = { x, y -> blockEmission(x + origin, y) },
        )
        private var built = false
        private val dirtyColumns = mutableSetOf<Int>()
        val changedEmitters = mutableSetOf<Long>()
        private var repairX: Int? = null
        private var repairY = 0

        fun invalidate(first: Int, last: Int) {
            for (x in maxOf(first, origin)..minOf(last, origin + candidate.width - 1)) dirtyColumns.add(x)
        }

        fun step(deadline: Long): Boolean {
            if (!built) {
                if (!rebuild.step(deadline)) return false
                built = true
            }
            // Reconcile changes made during the build, without throwing away its propagation.
            var work = 0
            while (repairX != null || dirtyColumns.isNotEmpty()) {
                val x = repairX ?: dirtyColumns.first().also {
                    dirtyColumns.remove(it)
                    repairX = it
                    repairY = 0
                }
                candidate.onCellChanged(x, repairY, isOpaque(x, repairY), blockEmission(x, repairY))
                if (++repairY == height) repairX = null
                if (++work == 32) {
                    work = 0
                    if (System.nanoTime() >= deadline) return false
                }
            }
            while (changedEmitters.isNotEmpty()) {
                val id = changedEmitters.first()
                changedEmitters.remove(id)
                val emitter = emitters[id]
                if (emitter != null && emitter.x in origin until origin + candidate.width) {
                    candidate.setTransientEmitter(id, emitter.x, emitter.y, emitter.level)
                } else {
                    candidate.clearTransientEmitter(id)
                }
                if (System.nanoTime() >= deadline) return changedEmitters.isEmpty()
            }
            return true
        }
    }

    fun contains(x: Int): Boolean = x.toLong() in originX.toLong() until originX.toLong() + WIDTH

    /** Budgeted updates choose visible strips first and retain every unfinished strip's progress. */
    fun update(
        centerX: Int,
        budgetNanos: Long = Long.MAX_VALUE,
        visibleMinX: Int = centerX,
        visibleMaxX: Int = centerX,
    ): Boolean {
        var changed = false
        if (abs(centerX.toLong() - (originX.toLong() + WIDTH / 2)) >= RECENTER_THRESHOLD) {
            val previous = grid
            val previousOrigin = originX
            originX = centerX - WIDTH / 2
            grid = LightGrid(WIDTH, height, originX, looping = false).apply {
                copyColumnsFrom(previous, maxOf(originX, previousOrigin), minOf(originX + WIDTH, previousOrigin + WIDTH) - 1)
                copyTransientEmittersFrom(previous)
                for ((id, emitter) in emitters) seedTransientEmitter(id, emitter.x, emitter.y, emitter.level)
            }
            readyColumns.removeAll { !contains(it) }
            pending.keys.removeAll { chunk -> !overlapsWindow(chunk) }
            scheduleMissing()
            changed = true
        }
        val deadline = if (budgetNanos == Long.MAX_VALUE) Long.MAX_VALUE else System.nanoTime() + budgetNanos
        while (pending.isNotEmpty()) {
            val next = pending.values.minBy { strip ->
                maxOf(visibleMinX - (strip.start + STRIP_WIDTH - 1), strip.start - visibleMaxX, 0)
            }
            if (next.step(deadline)) {
                val first = maxOf(originX, next.start)
                val last = minOf(originX + WIDTH, next.start + STRIP_WIDTH) - 1
                grid.copyColumnsFrom(next.candidate, first, last)
                readyColumns.addAll(first..last)
                pending.remove(next.chunk)
                changed = true
            }
            if (System.nanoTime() >= deadline) break
        }
        return changed
    }

    private fun overlapsWindow(chunk: Int): Boolean = chunk * STRIP_WIDTH < originX + WIDTH && (chunk + 1) * STRIP_WIDTH > originX

    private fun scheduleMissing() {
        for (x in originX until originX + WIDTH) {
            if (x !in readyColumns) schedule(Math.floorDiv(x, STRIP_WIDTH))
        }
    }

    private fun schedule(chunk: Int): Strip = pending.getOrPut(chunk) { Strip(chunk) }

    fun invalidateCell(x: Int) = invalidate(x, x)

    private fun invalidate(first: Int, last: Int) {
        val firstStrip = Math.floorDiv(originX, STRIP_WIDTH)
        val lastStrip = Math.floorDiv(originX + WIDTH - 1, STRIP_WIDTH)
        for (chunk in firstStrip..lastStrip) {
            val start = chunk * STRIP_WIDTH
            if (first < start + STRIP_WIDTH + HALO && last >= start - HALO) schedule(chunk).invalidate(first, last)
        }
    }

    fun setEmitter(id: Long, x: Int, y: Int, level: Int) {
        val value = if (level > 0) Emitter(x, y, level) else null
        val old = emitters[id]
        if (old == value) return
        if (value == null) emitters.remove(id) else emitters[id] = value
        if (value != null && contains(x)) grid.setTransientEmitter(id, x, y, level) else grid.clearTransientEmitter(id)
        // Include sources just outside the published window: their light can reach its edge.
        for (source in listOfNotNull(old, value)) {
            if (contains(source.x)) continue
            for (chunk in Math.floorDiv(originX, STRIP_WIDTH)..Math.floorDiv(originX + WIDTH - 1, STRIP_WIDTH)) {
                val start = chunk * STRIP_WIDTH
                if (source.x in start - HALO until start + STRIP_WIDTH + HALO) schedule(chunk).changedEmitters.add(id)
            }
        }
        pending.values.forEach { it.changedEmitters.add(id) }
    }

    fun covers(minX: Int, maxX: Int): Boolean = contains(minX) && contains(maxX) && (minX..maxX).all { it in readyColumns }

    override fun onChunkLoaded(chunkX: Int) = invalidateChunk(chunkX)

    override fun onChunkUnloaded(chunkX: Int) = invalidateChunk(chunkX)

    private fun invalidateChunk(chunkX: Int) {
        val start = chunkX * ChunkGenerator.CHUNK_W
        invalidate(start, start + ChunkGenerator.CHUNK_W - 1)
    }

    companion object {
        private const val WIDTH = 192
        private const val STRIP_WIDTH = 16
        private const val HALO = LightGrid.MAX_LEVEL
        private const val RECENTER_THRESHOLD = 32
    }
}
