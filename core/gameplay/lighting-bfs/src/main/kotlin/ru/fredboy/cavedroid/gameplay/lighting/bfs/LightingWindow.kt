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

    var grid = LightGrid(WIDTH, height, originX)
        private set

    init {
        update(0)
    }

    fun contains(x: Int): Boolean = x.toLong() in originX.toLong() until originX.toLong() + WIDTH

    /** Returns whether the overlay needs to pick up a rebuilt grid. */
    fun update(centerX: Int): Boolean {
        if (abs(centerX.toLong() - (originX.toLong() + WIDTH / 2)) >= RECENTER_THRESHOLD) {
            originX = centerX - WIDTH / 2
            grid = LightGrid(WIDTH, height, originX)
            dirty = true
        }
        if (!dirty) return false

        grid.rebuildAll(
            isOpaque = { x, y -> isOpaque(x + originX, y) },
            blockEmission = { x, y -> blockEmission(x + originX, y) },
        )
        dirty = false
        return true
    }

    override fun onChunkLoaded(chunkX: Int) = invalidateChunk(chunkX)

    override fun onChunkUnloaded(chunkX: Int) = invalidateChunk(chunkX)

    private fun invalidateChunk(chunkX: Int) {
        val start = chunkX.toLong() * ChunkGenerator.CHUNK_W
        val end = start + ChunkGenerator.CHUNK_W
        if (start < originX.toLong() + WIDTH && end > originX.toLong()) {
            dirty = true
        }
    }

    companion object {
        // Covers the viewport plus the light radius within the terrain streaming region.
        private const val WIDTH = 192
        private const val RECENTER_THRESHOLD = 32
    }
}
