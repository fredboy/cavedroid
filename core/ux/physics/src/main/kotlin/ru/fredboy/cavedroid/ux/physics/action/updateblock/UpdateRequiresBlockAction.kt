package ru.fredboy.cavedroid.ux.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.mob.factory.FallingBlockFactory
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateRequiresBlockAction.ACTION_KEY)
class UpdateRequiresBlockAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val fallingBlockFactory: FallingBlockFactory,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (gameWorld.getForeMap(x, y + 1).params.hasCollision.not()) {
            val block = gameWorld.getForeMap(x, y)

            if (block.params.isFallable) {
                gameWorld.resetForeMap(x, y)
                fallingBlockFactory.create(x.toFloat() + .5f, y.toFloat() + .5f, block)
            } else {
                gameWorld.destroyForeMap(x, y, true)
            }
        }
    }

    companion object {
        const val ACTION_KEY = "requires_block"
    }
}
