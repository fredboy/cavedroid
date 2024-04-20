package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.FallingSand
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class UpdateSandAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val shouldFall = gameWorld.getForeMap(x, y + 1).params.hasCollision.not()

        if (shouldFall) {
            gameWorld.resetForeMap(x, y)
            FallingSand(x * 16f, y * 16f)
                .apply { attachToController(mobsController) }
        }
    }

    companion object {
        const val BLOCK_KEY = "sand"
    }
}