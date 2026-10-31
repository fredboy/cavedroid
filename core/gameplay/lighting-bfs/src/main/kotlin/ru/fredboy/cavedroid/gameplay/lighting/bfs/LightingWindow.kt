package ru.fredboy.cavedroid.gameplay.lighting.bfs

import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.store.ChunkListener
import kotlin.math.abs

/** Keeps the light grid in sync with streamed terrain, batching chunk events until the next update. */
internal class LightingWindow(
    private val height: Int,
    private val isOpaque: (Int, Int) -> Boolean,
    private val blockEmission: (Int, Int) -> Int,
) : ChunkListener {

    private var originX = -WIDTH / 2
    private var dirty = true
    private var pending: Pending? = null
    private data class Emitter(val x: Int, val y: Int, val level: Int)
    private val emitters = mutableMapOf<Long, Emitter>()

    private class Pending(val origin: Int, val grid: LightGrid, val rebuild: LightGrid.Rebuild, val emitters: Long)

    var grid = LightGrid(WIDTH, height, originX)
        private set

    init {
        update(0)
    }

    fun contains(x: Int): Boolean = x.toLong() in originX.toLong() until originX.toLong() + WIDTH

    /** Returns whether the overlay needs to pick up a rebuilt grid. */
    fun update(centerX: Int, budgetNanos: Long = Long.MAX_VALUE): Boolean {
        val requestedOrigin = pending?.origin ?: originX
        val recenter = abs(centerX.toLong() - (requestedOrigin.toLong() + WIDTH / 2)) >= RECENTER_THRESHOLD
        if (recenter || dirty || pending?.emitters?.let { it != grid.transientRevision } == true) {
            val target = if (recenter) centerX - WIDTH / 2 else requestedOrigin
            val candidate = LightGrid(WIDTH, height, target)
            candidate.copyTransientEmittersFrom(grid)
            for ((id, emitter) in emitters) candidate.seedTransientEmitter(id, emitter.x, emitter.y, emitter.level)
            pending = Pending(
                target,
                candidate,
                candidate.beginRebuild(
                    isOpaque = { x, y -> isOpaque(x + target, y) },
                    blockEmission = { x, y -> blockEmission(x + target, y) },
                ),
                grid.transientRevision,
            )
        }
        dirty = false
        val build = pending ?: return false
        val deadline = if (budgetNanos == Long.MAX_VALUE) Long.MAX_VALUE else System.nanoTime() + budgetNanos
        if (!build.rebuild.step(deadline)) return false
        grid = build.grid
        originX = build.origin
        pending = null
        return true
    }

    fun invalidateCell(x: Int) {
        val target = pending?.origin ?: return
        if (x.toLong() in target.toLong() until target.toLong() + WIDTH) dirty = true
    }

    fun setEmitter(id: Long, x: Int, y: Int, level: Int) {
        val value = if (level > 0) Emitter(x, y, level) else null
        if (emitters[id] == value) return
        if (value == null) emitters.remove(id) else emitters[id] = value
        if (pending != null) dirty = true
        if (value != null && contains(x)) grid.setTransientEmitter(id, x, y, level) else grid.clearTransientEmitter(id)
    }

    fun covers(minX: Int, maxX: Int): Boolean = contains(minX) && contains(maxX)

    override fun onChunkLoaded(chunkX: Int) = invalidateChunk(chunkX)

    override fun onChunkUnloaded(chunkX: Int) = invalidateChunk(chunkX)

    private fun invalidateChunk(chunkX: Int) {
        val start = chunkX.toLong() * ChunkGenerator.CHUNK_W
        val end = start + ChunkGenerator.CHUNK_W
        val target = pending?.origin ?: originX
        if (start < target.toLong() + WIDTH && end > target.toLong() || start < originX.toLong() + WIDTH && end > originX.toLong()) {
            dirty = true
        }
    }

    companion object {
        // Covers the viewport plus the light radius within the terrain streaming region.
        private const val WIDTH = 192
        private const val RECENTER_THRESHOLD = 32
    }
}
