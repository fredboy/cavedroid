package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.UniqueQueue
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.getRequiresBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import java.util.Queue
import javax.inject.Inject

@GameScope
class GameWorldBlocksLogicControllerTask @Inject constructor(
    private val gameWorld: GameWorld,
    private val updateBlockActions: Map<String, @JvmSuppressWildcards IUpdateBlockAction>,
) : BaseGameWorldControllerTask() {

    private val queueLock = Any()

    private val dirtyChunks: Queue<Pair<Int, Int>> = UniqueQueue()

    private val onBlockPlacedListener = OnBlockPlacedListener { block, x, y, layer ->
        if (block.isNone() || layer == Layer.BACKGROUND) {
            return@OnBlockPlacedListener
        }

        markChunksDirtyIfNeed(x, y, "${block.params.key} placed")
    }

    private val onBlockDestroyedListener = OnBlockDestroyedListener { block, x, y, layer, _, _ ->
        if (block.isNone() || layer == Layer.BACKGROUND) {
            return@OnBlockDestroyedListener
        }

        markChunksDirtyIfNeed(x, y, "${block.params.key} destroyed")
    }

    init {
        gameWorld.addBlockPlacedListener(onBlockPlacedListener)
        gameWorld.addBlockDestroyedListener(onBlockDestroyedListener)
    }

    private fun markChunksDirtyIfNeed(x: Int, y: Int, reason: String) {
        val chunkX = x - (x % CHUNK_SIZE)
        val chunkY = y - (y % CHUNK_SIZE)
        val coordinates = chunkX to chunkY

        val localX = x % CHUNK_SIZE
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
                dirtyChunks.offer(neighborCoords).ifTrue {
                    Gdx.app.debug(TAG, "Marking chunk as dirty: $neighborCoords. Reason: $reason")
                }
            }
        }
    }

    private fun updateBlock(x: Int, y: Int): Boolean {
        val block = gameWorld.getForeMap(x, y)

        if (block.isNone()) {
            return false
        }

        val blockKey = block.params.key
        val action = updateBlockActions[blockKey]
            ?: updateBlockActions.getRequiresBlockAction()
                .takeIf { block.params.run { requiresBlock || isFallable } }

        return action?.update(x, y) != null
    }

    override fun exec() {
        val (startX, startY) = synchronized(queueLock) {
            dirtyChunks.poll()
        } ?: return

        Gdx.app.debug(TAG, "Updating chunk ($startX, $startY)")

        val updateCalls = sequence {
            for (y in startY + CHUNK_SIZE - 1 downTo startY) {
                for (x in startX..<startX + CHUNK_SIZE) {
                    yield(updateBlock(x, y))
                }
            }
        }.count { it }

        Gdx.app.debug(TAG, "Chunk ($startX, $startY) updated with $updateCalls update() calls")
    }

    override fun dispose() {
        super.dispose()
        gameWorld.removeBlockPlacedListener(onBlockPlacedListener)
        gameWorld.removeBlockDestroyedListener(onBlockDestroyedListener)
    }

    companion object {
        private const val TAG = "GameWorldBlocksLogicControllerTask"

        private const val CHUNK_SIZE = 16

        const val WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC = 0.25f
    }
}
