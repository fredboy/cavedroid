package ru.fredboy.cavedroid.game.world.store

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome

/**
 * A single [ChunkGenerator.CHUNK_W]-wide column slice of an infinite world, covering world columns
 * `[chunkX * CHUNK_W, +CHUNK_W)` and the full world height.
 *
 * [dirty] is set whenever a block is modified after generation/load, so streaming only persists
 * chunks that diverge from what the generator would reproduce.
 */
class Chunk(
    val chunkX: Int,
    val fore: Array<Array<Block>>,
    val back: Array<Array<Block>>,
    val biomes: Array<Biome>,
    var dirty: Boolean = false,
)

/** Notified by a streaming [WorldBlockStore] when chunks enter or leave memory. */
interface ChunkListener {
    fun onChunkLoaded(chunkX: Int)
    fun onChunkUnloaded(chunkX: Int)
}
