package ru.fredboy.cavedroid.game.world.store

import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

/**
 * Unbounded, Minecraft-like world. Holds only the chunks near the current view; the rest are
 * generated lazily (or loaded from disk) when streamed in, and evicted when the view moves away.
 *
 * X is identity (no wrap); columns extend infinitely in both directions including negatives.
 * Reads outside the resident region return air — the streaming window ([LOAD_RADIUS]) is sized so
 * everything the renderer and physics touch is always resident.
 *
 * Streaming is driven from [update] (called every frame on the main thread via `GameWorld.update`),
 * keyed off the camera centre so generation/eviction happen around what the player is looking at.
 */
class InfiniteBlockStore(
    private val itemsRepository: ItemsRepository,
    override val generatorConfig: WorldGeneratorConfig,
    private val gameContextRepository: GameContextRepository,
    private val chunkLoader: (chunkX: Int) -> Chunk? = { null },
    private val chunkPersister: (Chunk) -> Unit = { },
) : WorldBlockStore {

    private val generator = ChunkGenerator(generatorConfig, itemsRepository)
    private val chunks = HashMap<Int, Chunk>()
    private val listeners = mutableListOf<ChunkListener>()

    private var lastCenterChunk = Int.MIN_VALUE

    override val width: Int = Int.MAX_VALUE
    override val height: Int = generatorConfig.height
    override val isInfinite: Boolean = true

    override val foreMap: Array<Array<Block>>
        get() = throw UnsupportedOperationException("Infinite world has no whole-map array; iterate loaded chunks")

    override val backMap: Array<Array<Block>>
        get() = throw UnsupportedOperationException("Infinite world has no whole-map array; iterate loaded chunks")

    override val biomes: Array<Biome>
        get() = throw UnsupportedOperationException("Infinite world has no whole-map array; use getBiomeAt")

    init {
        // Pre-load the spawn region so the first render and spawn search have terrain to work with.
        stream(centerChunk = 0)
        lastCenterChunk = 0
    }

    override fun transformX(x: Int): Int = x

    override fun isInBounds(x: Int, y: Int): Boolean = y in 0 until height

    override fun getBlock(x: Int, y: Int, layer: Layer): Block {
        if (y !in 0 until height) return itemsRepository.fallbackBlock
        val chunk = chunks[chunkIndex(x)] ?: return itemsRepository.fallbackBlock
        val lx = localX(x)
        return when (layer) {
            Layer.FOREGROUND -> chunk.fore[lx][y]
            Layer.BACKGROUND -> chunk.back[lx][y]
        }
    }

    override fun setBlock(x: Int, y: Int, layer: Layer, value: Block) {
        if (y !in 0 until height) return
        val chunk = chunks[chunkIndex(x)] ?: return
        val lx = localX(x)
        when (layer) {
            Layer.FOREGROUND -> chunk.fore[lx][y] = value
            Layer.BACKGROUND -> chunk.back[lx][y] = value
        }
        chunk.dirty = true
    }

    override fun getBiomeAt(x: Int): Biome {
        // Biome is a pure function of x, so answer even for chunks that are not resident.
        return chunks[chunkIndex(x)]?.biomes?.get(localX(x)) ?: generator.biomeAt(x)
    }

    override fun update() {
        val visible = gameContextRepository.getCameraContext().visibleWorld
        val centerX = (visible.x + visible.width / 2f).toInt()
        val centerChunk = chunkIndex(centerX)
        if (centerChunk == lastCenterChunk) return
        lastCenterChunk = centerChunk
        stream(centerChunk)
    }

    override fun addChunkListener(listener: ChunkListener) {
        listeners.add(listener)
    }

    override fun removeChunkListener(listener: ChunkListener) {
        listeners.remove(listener)
    }

    override fun forEachLoadedChunk(action: (chunkX: Int) -> Unit) {
        // Copy keys to tolerate listeners that mutate the chunk set.
        chunks.keys.toList().forEach(action)
    }

    override fun dispose() {
        chunks.values.forEach { chunk -> if (chunk.dirty) chunkPersister(chunk) }
        chunks.clear()
        listeners.clear()
    }

    private fun stream(centerChunk: Int) {
        for (cx in centerChunk - LOAD_RADIUS..centerChunk + LOAD_RADIUS) {
            ensureChunk(cx)
        }

        val keep = (centerChunk - UNLOAD_RADIUS)..(centerChunk + UNLOAD_RADIUS)
        chunks.keys.filter { it !in keep }.forEach(::evictChunk)
    }

    private fun ensureChunk(chunkX: Int): Chunk {
        chunks[chunkX]?.let { return it }

        val chunk = chunkLoader(chunkX) ?: generator.generateChunk(chunkX).let { generated ->
            Chunk(
                chunkX = chunkX,
                fore = generated.foreMap,
                back = generated.backMap,
                biomes = generated.biomes,
            )
        }

        chunks[chunkX] = chunk
        listeners.forEach { it.onChunkLoaded(chunkX) }
        return chunk
    }

    private fun evictChunk(chunkX: Int) {
        val chunk = chunks.remove(chunkX) ?: return
        listeners.forEach { it.onChunkUnloaded(chunkX) }
        if (chunk.dirty) {
            chunkPersister(chunk)
        }
    }

    private fun chunkIndex(x: Int): Int = Math.floorDiv(x, ChunkGenerator.CHUNK_W)

    private fun localX(x: Int): Int = Math.floorMod(x, ChunkGenerator.CHUNK_W)

    companion object {
        /** Chunks loaded on each side of the centre chunk. Must cover the viewport plus a buffer. */
        private const val LOAD_RADIUS = 4

        /** Chunks beyond this distance are evicted. Hysteresis vs. [LOAD_RADIUS] avoids thrashing. */
        private const val UNLOAD_RADIUS = 6
    }
}
