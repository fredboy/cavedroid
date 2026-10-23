package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateFenceAction.ACTION_KEY)
class UpdateFenceAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKey: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        // An unloaded neighbor is unknown, not air. Its load will queue another update.
        if (!gameWorld.isChunkLoadedAt(x - 1) || !gameWorld.isChunkLoadedAt(x + 1)) return

        updateLayer(x, y, gameWorld::getForeMap) { block -> gameWorld.setForeMap(x, y, block) }
        updateLayer(x, y, gameWorld::getBackMap) { block -> gameWorld.setBackMap(x, y, block) }
    }

    private fun updateLayer(x: Int, y: Int, getBlock: (Int, Int) -> Block, setBlock: (Block) -> Unit) {
        val fence = getBlock(x, y).takeIf(FenceBlocks::isFence) ?: return
        val key = FenceBlocks.connectedKey(fence, getBlock(x - 1, y), getBlock(x + 1, y))
        if (key != fence.params.key) {
            setBlock(getBlockByKey[key])
        }
    }

    companion object {
        const val ACTION_KEY = FenceBlocks.UPDATE_ACTION_KEY
    }
}
