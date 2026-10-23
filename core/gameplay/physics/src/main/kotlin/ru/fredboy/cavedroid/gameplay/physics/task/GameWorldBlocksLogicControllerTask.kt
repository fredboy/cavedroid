package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceMetric
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.utils.UniqueQueue
import ru.fredboy.cavedroid.common.utils.floorDiv
import ru.fredboy.cavedroid.common.utils.floorMod
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import ru.fredboy.cavedroid.game.world.store.ChunkListener
import ru.fredboy.cavedroid.gameplay.physics.action.getRequiresBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.UpdateFenceAction
import java.util.Queue
import javax.inject.Inject

@GameScope
class GameWorldBlocksLogicControllerTask @Inject constructor(
    private val gameWorld: GameWorld,
    private val updateBlockActions: Map<String, @JvmSuppressWildcards IUpdateBlockAction>,
    performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
) : BaseGameWorldControllerTask(performanceMonitor, PerformanceMetric.TASK_BLOCKS),
    ChunkListener {

    private val queueLock = Any()

    private val dirtyChunks: Queue<Pair<Int, Int>> = UniqueQueue()

    private val onBlockPlacedListener = OnBlockPlacedListener { block, x, y, _ ->
        if (block.isNone()) {
            return@OnBlockPlacedListener
        }

        markChunksDirtyIfNeed(x, y, "${block.params.key} placed")
    }

    private val onBlockDestroyedListener = OnBlockDestroyedListener { block, x, y, _, _, _ ->
        if (block.isNone()) {
            return@OnBlockDestroyedListener
        }

        markChunksDirtyIfNeed(x, y, "${block.params.key} destroyed")
    }

    init {
        gameWorld.addBlockPlacedListener(onBlockPlacedListener)
        gameWorld.addBlockDestroyedListener(onBlockDestroyedListener)
        gameWorld.addChunkListener(this)
        gameWorld.forEachLoadedChunk(::onChunkLoaded)
    }

    override fun onChunkLoaded(chunkX: Int) {
        val startX = chunkX * ChunkGenerator.CHUNK_W

        synchronized(queueLock) {
            for (x in startX until startX + ChunkGenerator.CHUNK_W step CHUNK_SIZE) {
                for (y in 0 until gameWorld.height step CHUNK_SIZE) {
                    dirtyChunks.offer(x to y).ifTrue {
                        logger.v { "Marking loaded chunk as dirty: ($x, $y)" }
                    }
                }
            }
        }

        for (y in 0 until gameWorld.height step CHUNK_SIZE) {
            markChunksDirtyIfNeed(startX, y, "neighbor chunk loaded")
            markChunksDirtyIfNeed(startX + ChunkGenerator.CHUNK_W - 1, y, "neighbor chunk loaded")
        }
    }

    override fun onChunkUnloaded(chunkX: Int) {
        val startX = chunkX * ChunkGenerator.CHUNK_W

        synchronized(queueLock) {
            for (x in startX until startX + ChunkGenerator.CHUNK_W step CHUNK_SIZE) {
                for (y in 0 until gameWorld.height step CHUNK_SIZE) {
                    dirtyChunks.remove(x to y).ifTrue {
                        logger.v { "Removing unloaded chunk: ($x, $y)" }
                    }
                }
            }
        }
    }

    private fun markChunksDirtyIfNeed(x: Int, y: Int, reason: String) {
        val chunkX = (x floorDiv CHUNK_SIZE) * CHUNK_SIZE
        val chunkY = y - (y % CHUNK_SIZE)
        val coordinates = chunkX to chunkY

        val localX = x - chunkX
        val localY = y % CHUNK_SIZE

        val neighbors = buildList {
            add(coordinates)

            if (localX == 0) {
                add(chunkX - CHUNK_SIZE to chunkY)
            }

            if (localX == CHUNK_SIZE - 1) {
                add(chunkX + CHUNK_SIZE to chunkY)
            }

            if (localY == 0) {
                add(chunkX to chunkY - CHUNK_SIZE)
            }

            if (localY == CHUNK_SIZE - 1) {
                add(chunkX to chunkY + CHUNK_SIZE)
            }
        }

        synchronized(queueLock) {
            neighbors.forEach { neighborCoords ->
                val (neighborX, neighborY) = neighborCoords
                if (neighborY !in 0 until gameWorld.height || !gameWorld.isChunkLoadedAt(neighborX)) return@forEach
                val normalizedX = if (gameWorld.isInfinite) neighborX else neighborX floorMod gameWorld.width
                dirtyChunks.offer(normalizedX to neighborY).ifTrue {
                    logger.v { "Marking chunk as dirty: $neighborCoords. Reason: $reason" }
                }
            }
        }
    }

    private fun updateBlock(x: Int, y: Int): Boolean {
        val block = gameWorld.getForeMap(x, y)
        val hasFence = FenceBlocks.isFence(block) || FenceBlocks.isFence(gameWorld.getBackMap(x, y))
        if (hasFence) {
            updateBlockActions[UpdateFenceAction.ACTION_KEY]?.update(x, y)
        }

        if (block.isNone() || FenceBlocks.isFence(block)) {
            return hasFence
        }

        val blockKey = block.params.key
        val action = updateBlockActions[block.params.updateActionKey ?: blockKey]
            ?: updateBlockActions.getRequiresBlockAction()
                .takeIf { block.params.run { requiresBlock || isFallable || requiresBackground } }

        return action?.update(x, y) != null
    }

    override fun exec() {
        val (startX, startY) = synchronized(queueLock) {
            dirtyChunks.poll()
        } ?: return

        logger.v { "Updating chunk ($startX, $startY)" }

        val updateCalls = sequence {
            for (y in startY + CHUNK_SIZE - 1 downTo startY) {
                for (x in startX..<startX + CHUNK_SIZE) {
                    yield(updateBlock(x, y))
                }
            }
        }.count { it }

        logger.v { "Chunk ($startX, $startY) updated with $updateCalls update() calls" }
    }

    override fun dispose() {
        super.dispose()
        gameWorld.removeBlockPlacedListener(onBlockPlacedListener)
        gameWorld.removeBlockDestroyedListener(onBlockDestroyedListener)
        gameWorld.removeChunkListener(this)
    }

    companion object {
        private const val TAG = "GameWorldBlocksLogicControllerTask"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val CHUNK_SIZE = 16

        const val WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC = 0.025f
    }
}
