package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.FallingBlock
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUpdateBlockAction
import ru.deadsoftware.cavedroid.misc.utils.px
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateRequiresBlockAction.ACTION_KEY)
class UpdateRequiresBlockAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (gameWorld.getForeMap(x, y + 1).params.hasCollision.not()) {
            val block = gameWorld.getForeMap(x, y)

            if (block.params.isFallable) {
                gameWorld.resetForeMap(x, y)
                FallingBlock(block.params.key, x.px, y.px)
                    .attachToController(mobsController)
            } else {
                gameWorld.destroyForeMap(x, y)
            }
        }
    }

    companion object {
        const val ACTION_KEY = "requires_block"
    }
}