package ru.fredboy.cavedroid.game.world.store

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.utils.floorToInt
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import ru.fredboy.cavedroid.game.world.store.InfiniteBlockStore.Companion.LOAD_RADIUS

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
    private val appDispatchers: AppDispatchers,
    private val chunkLoader: (chunkX: Int) -> Chunk? = { null },
    private val chunkPersister: (Chunk) -> Unit = { },
) : WorldBlockStore {

    private val coroutineScope = CoroutineScope(SupervisorJob() + appDispatchers.main)

    private val generator = ChunkGenerator(generatorConfig, itemsRepository)

    // Resident terrain, requests and listeners are exclusively owned by the main thread.
    private val chunks = HashMap<Int, Chunk>()
    private val loads = HashMap<Int, LoadRequest>()
    private val pendingSaves = HashMap<Int, PendingSave>()
    private val storageLock = Any()
    private val listeners = mutableListOf<ChunkListener>()

    private var lastCenterChunk = Int.MIN_VALUE

    override val width: Int = Int.MAX_VALUE
    override val height: Int = generatorConfig.height
    override val start: Int = Int.MIN_VALUE
    override val end: Int = Int.MAX_VALUE
    override val isInfinite: Boolean = true

    override val foreMap: Array<Array<Block>>
        get() = throw UnsupportedOperationException("Infinite world has no whole-map array; iterate loaded chunks")

    override val backMap: Array<Array<Block>>
        get() = throw UnsupportedOperationException("Infinite world has no whole-map array; iterate loaded chunks")

    override val biomes: Array<Biome>
        get() = throw UnsupportedOperationException("Infinite world has no whole-map array; use getBiomeAt")

    init {
        // Pre-load the spawn region so the first render and spawn search have terrain to work with.
        for (cx in -LOAD_RADIUS..LOAD_RADIUS) chunks[cx] = loadOrGenerate(cx)
        lastCenterChunk = 0
    }

    override fun transformX(x: Int): Int = x

    override fun isInBounds(x: Int, y: Int): Boolean = y in 0 until height

    override fun isChunkLoaded(x: Int): Boolean = chunkIndex(x) in chunks

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
        val centerX = (visible.x + visible.width / 2f).floorToInt()
        val centerChunk = chunkIndex(centerX)
        if (centerChunk == lastCenterChunk) return
        lastCenterChunk = centerChunk

        val wanted = (centerChunk - LOAD_RADIUS)..(centerChunk + LOAD_RADIUS)
        val keep = (centerChunk - UNLOAD_RADIUS)..(centerChunk + UNLOAD_RADIUS)
        loads.keys.filterNot { it in wanted }.forEach { loads.remove(it)?.job?.cancel() }
        chunks.keys.filterNot { it in keep }.forEach(::evictChunk)

        if (centerChunk !in chunks) {
            // Initial spawn at a saved position and teleports need terrain before this frame's step.
            for (cx in wanted) {
                if (cx in chunks) continue
                loads.remove(cx)?.job?.cancel()
                val chunk = pendingSaves[cx]?.chunk?.let(::copyChunk) ?: loadOrGenerate(cx)
                publish(chunk)
            }
        } else {
            for (cx in wanted) {
                if (cx !in chunks && cx !in loads) requestChunk(cx)
            }
        }
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

    override fun flushDirtyChunks() {
        for (chunk in chunks.values) {
            if (chunk.dirty) {
                val save = snapshotSave(chunk)
                persist(save)
                chunk.dirty = false
                pendingSaves.remove(chunk.chunkX)
            }
        }
        // Evicted terrain may still be queued for I/O when a full save or disposal occurs.
        for (save in pendingSaves.values.toList()) {
            persist(save)
            pendingSaves.remove(save.chunk.chunkX)
        }
    }

    override fun dispose() {
        coroutineScope.cancel()
        loads.clear()
        flushDirtyChunks()
        chunks.clear()
        listeners.clear()
    }

    private fun requestChunk(chunkX: Int) {
        val request = LoadRequest()
        loads[chunkX] = request
        request.job = coroutineScope.launch(start = CoroutineStart.LAZY) {
            try {
                val saved = pendingSaves[chunkX]?.chunk
                val chunk = if (saved != null) {
                    copyChunk(saved)
                } else {
                    withContext(appDispatchers.background) { loadOrGenerate(chunkX) }
                }
                if (loads[chunkX] === request) {
                    loads.remove(chunkX)
                    publish(chunk)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                logger.e(error) { "Failed to load chunk $chunkX" }
                lastCenterChunk = Int.MIN_VALUE
            } finally {
                if (loads[chunkX] === request) loads.remove(chunkX)
            }
        }.also { it.start() }
    }

    private fun loadOrGenerate(chunkX: Int): Chunk {
        val loaded = synchronized(storageLock) { chunkLoader(chunkX) }
        return loaded ?: generator.generateChunk(chunkX).let { generated ->
            Chunk(chunkX, generated.foreMap, generated.backMap, generated.biomes)
        }
    }

    private fun publish(chunk: Chunk) {
        chunks[chunk.chunkX] = chunk
        listeners.toList().forEach { it.onChunkLoaded(chunk.chunkX) }
    }

    private fun evictChunk(chunkX: Int) {
        val chunk = chunks.remove(chunkX) ?: return
        if (chunk.dirty) {
            val save = snapshotSave(chunk)
            coroutineScope.launch(appDispatchers.io) {
                try {
                    persist(save)
                    withContext(appDispatchers.main) {
                        if (pendingSaves[chunkX] === save) pendingSaves.remove(chunkX)
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    // Retain the snapshot for a later reload, explicit save or disposal retry.
                    logger.e(error) { "Failed to persist chunk $chunkX" }
                }
            }
        }
        listeners.toList().forEach { it.onChunkUnloaded(chunkX) }
    }

    private fun snapshotSave(chunk: Chunk): PendingSave {
        val sequence = pendingSaves[chunk.chunkX]?.sequence ?: SaveSequence()
        return PendingSave(copyChunk(chunk), sequence, ++sequence.nextVersion).also {
            pendingSaves[chunk.chunkX] = it
        }
    }

    private fun persist(save: PendingSave) {
        synchronized(storageLock) {
            // A delayed eviction write must never overwrite a newer full-save/disposal snapshot.
            if (save.version <= save.sequence.persistedVersion) return
            chunkPersister(save.chunk)
            save.sequence.persistedVersion = save.version
        }
    }

    private fun copyChunk(chunk: Chunk): Chunk = Chunk(
        chunkX = chunk.chunkX,
        fore = Array(chunk.fore.size) { chunk.fore[it].copyOf() },
        back = Array(chunk.back.size) { chunk.back[it].copyOf() },
        biomes = chunk.biomes.copyOf(),
        dirty = chunk.dirty,
    )

    private fun chunkIndex(x: Int): Int = Math.floorDiv(x, ChunkGenerator.CHUNK_W)

    private fun localX(x: Int): Int = Math.floorMod(x, ChunkGenerator.CHUNK_W)

    private class LoadRequest {
        var job: Job? = null
    }

    private class SaveSequence {
        var nextVersion = 0L // Main-thread only.
        var persistedVersion = 0L // Protected by storageLock.
    }

    private class PendingSave(val chunk: Chunk, val sequence: SaveSequence, val version: Long)

    companion object {
        private const val TAG = "InfiniteBlockStore"
        private val logger = Logger.withTag(TAG)

        // Chunks loaded on each side of the centre chunk. Must cover the viewport plus the lighting
        // window (±96 blocks ≈ 6 chunks) so light rebuilds read real terrain.
        private const val LOAD_RADIUS = 6

        /** Chunks beyond this distance are evicted. Hysteresis vs. [LOAD_RADIUS] avoids thrashing. */
        private const val UNLOAD_RADIUS = 8
    }
}
