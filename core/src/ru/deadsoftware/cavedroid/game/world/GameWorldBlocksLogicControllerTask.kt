package ru.deadsoftware.cavedroid.game.world

import com.badlogic.gdx.utils.Timer.Task
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.actions.getRequiresBlockAction
import ru.deadsoftware.cavedroid.game.actions.updateblock.IUpdateBlockAction
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import javax.inject.Inject

@GameScope
class GameWorldBlocksLogicControllerTask @Inject constructor(
    private val gameWorld: GameWorld,
    private val updateBlockActions: Map<String, @JvmSuppressWildcards IUpdateBlockAction>,
    private val mobsController: MobsController,
) : Task() {

    private var currentRelativeChunk = 0

    private fun getChunkStart(): Int {
        val playerX = mobsController.player.mapX
        val playerChunk = playerX / CHUNK_WIDTH
        val currentChunk = playerChunk - CHUNKS / 2 + currentRelativeChunk

        return currentChunk * 16
    }

    private fun updateBlock(x: Int, y: Int) {
        val block = gameWorld.getForeMapBlock(x, y)
        val blockKey = block.params.key
        val action = updateBlockActions[blockKey]
            ?: updateBlockActions.getRequiresBlockAction().takeIf { block.params.requiresBlock }

        action?.update(x, y)
    }

    override fun run() {
        val startX = getChunkStart()

        for (y in gameWorld.height downTo 0) {
            for (x in startX ..< startX + CHUNK_WIDTH) {
                updateBlock(x, y)
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
