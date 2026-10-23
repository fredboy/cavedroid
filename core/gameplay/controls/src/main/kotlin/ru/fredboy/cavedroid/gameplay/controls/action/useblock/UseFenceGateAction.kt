package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseFenceGateAction.ACTION_KEY)
class UseFenceGateAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKey: GetBlockByKeyUseCase,
    private val mobController: MobController,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int): Boolean {
        if (!FenceBlocks.isGate(block)) return false
        val target = getBlockByKey[FenceBlocks.toggledGateKey(block)]
        if (target.params.hasCollision) {
            val rectangle = target.getRectangle(x, y)
            if (mobController.player.hitbox.overlaps(rectangle) || mobController.mobs.any { it.hitbox.overlaps(rectangle) }) {
                return false
            }
        }
        gameWorld.setForeMap(x, y, target)
        return true
    }

    companion object {
        const val ACTION_KEY = FenceBlocks.USE_GATE_ACTION_KEY
    }
}
