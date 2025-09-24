package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.getRequiresBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import javax.inject.Inject

@GameScope
class GameWorldBlocksLogicControllerTask @Inject constructor(
    private val gameWorld: GameWorld,
    private val updateBlockActions: Map<String, @JvmSuppressWildcards IUpdateBlockAction>,
    private val mobController: MobController,
) : BaseGameWorldControllerTask() {

    private var currentRelativeChunk = 0

    private val dirtyChunks = mutableSetOf<Int>()

    private val onBlockPlacedListener = OnBlockPlacedListener { _, x, _, layer ->
        if (layer == Layer.BACKGROUND) {
            return@OnBlockPlacedListener
        }

        dirtyChunks.add(x - (x % CHUNK_WIDTH))
    }

    init {
        gameWorld.addBlockPlacedListener(onBlockPlacedListener)
    }

    private fun getChunkStart(): Int {
        val playerX = mobController.player.mapX
        val playerChunk = playerX / CHUNK_WIDTH
        val currentChunk = playerChunk - CHUNKS / 2 + currentRelativeChunk

        return currentChunk * 16
    }

    private fun updateBlock(x: Int, y: Int) {
        val block = gameWorld.getForeMap(x, y)

        if (block.isNone()) {
            return
        }

        val blockKey = block.params.key
        val action = updateBlockActions[blockKey]
            ?: updateBlockActions.getRequiresBlockAction().takeIf { block.params.run { requiresBlock || isFallable } }

        action?.update(x, y)
    }

    override fun exec() {
        val startX = getChunkStart()

        if (startX in dirtyChunks) {
            dirtyChunks.remove(startX)

            for (y in gameWorld.height downTo 0) {
                for (x in startX..<startX + CHUNK_WIDTH) {
                    updateBlock(x, y)
                }
            }
        }

        currentRelativeChunk = (currentRelativeChunk + 1) % CHUNKS
    }

    companion object {
        private const val TAG = "GameWorldBlocksLogicControllerTask"

        private const val CHUNK_WIDTH = 16
        private const val CHUNKS = 3

        const val WORLD_BLOCKS_LOGIC_UPDATE_INTERVAL_SEC = .1f
    }
}
