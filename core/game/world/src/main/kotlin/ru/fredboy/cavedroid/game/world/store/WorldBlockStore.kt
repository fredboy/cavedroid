package ru.fredboy.cavedroid.game.world.store

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

/**
 * Abstracts block storage and X-coordinate semantics out of [ru.fredboy.cavedroid.game.world.GameWorld].
 *
 * Two topologies implement this:
 * - [FiniteLoopingBlockStore] — the classic fixed-width, horizontally-wrapping world backed by
 *   dense arrays.
 * - the infinite store (added in a later milestone) — a sparse, lazily-generated, streamed world
 *   with identity X (no wrap, negative coordinates allowed).
 *
 * [GameWorld] keeps world-level behaviour (listeners, slab logic, day/night, physics step) and
 * delegates all raw block access here.
 */
interface WorldBlockStore : Disposable {

    val generatorConfig: WorldGeneratorConfig

    /**
     * Logical world width. For looping worlds this is the wrap period; for infinite worlds it is
     * [Int.MAX_VALUE] (there is no period). Use [isInfinite] to branch on topology rather than
     * comparing against this value.
     */
    @Deprecated(message = "Do not use! Use start/end to compare against world bounds")
    val width: Int

    /**
     * The minimal x coordinate of the world
     */
    val start: Int

    /**
     * The maximum x coordinate of the world
     */
    val end: Int

    val height: Int

    val isInfinite: Boolean

    /**
     * The dense foreground map. Only valid for finite worlds; infinite stores throw, since there is
     * no whole-world array. Consumers that must support infinite worlds iterate loaded chunks
     * instead of reading this directly.
     */
    val foreMap: Array<Array<Block>>

    val backMap: Array<Array<Block>>

    val biomes: Array<Biome>

    /** Maps an arbitrary world-x into canonical storage space (modulo width for looping; identity for infinite). */
    fun transformX(x: Int): Int

    /** True if (x, y) addresses an in-bounds, currently-addressable cell. */
    fun isInBounds(x: Int, y: Int): Boolean

    fun getBlock(x: Int, y: Int, layer: Layer): Block

    fun setBlock(x: Int, y: Int, layer: Layer, value: Block)

    fun getBiomeAt(x: Int): Biome

    /**
     * Per-frame hook (called from `GameWorld.update`). Streaming stores load/evict chunks around the
     * current view here. No-op for finite worlds.
     */
    fun update() = Unit

    /** Registers a listener notified when chunks are loaded/unloaded. No-op for finite worlds. */
    fun addChunkListener(listener: ChunkListener) = Unit

    fun removeChunkListener(listener: ChunkListener) = Unit

    /** Iterates the chunk indices currently resident in memory. No-op for finite worlds. */
    fun forEachLoadedChunk(action: (chunkX: Int) -> Unit) = Unit

    /** Persists every resident chunk that has unsaved edits. No-op for finite worlds. */
    fun flushDirtyChunks() = Unit
}
