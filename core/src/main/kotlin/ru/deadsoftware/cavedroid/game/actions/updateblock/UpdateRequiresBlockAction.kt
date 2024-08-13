package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUpdateBlockAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.model.FallingBlock
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateRequiresBlockAction.ACTION_KEY)
class UpdateRequiresBlockAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (gameWorld.getForeMap(x, y + 1).params.hasCollision.not()) {
            val block = gameWorld.getForeMap(x, y)

            if (block.params.isFallable) {
                gameWorld.resetForeMap(x, y)
                FallingBlock(block, x.px, y.px)
                    .attachToController(mobController)
            } else {
                gameWorld.destroyForeMap(x, y)
            }
        }
    }

    companion object {
        const val ACTION_KEY = "requires_block"
    }
}