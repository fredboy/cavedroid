package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class UpdateRequiresBlockAction @Inject constructor(
    private val gameWorld: GameWorld,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (gameWorld.getForeMap(x, y + 1).params.hasCollision.not()) {
            gameWorld.destroyForeMap(x, y)
        }
    }

    companion object {
        const val ACTION_KEY = "requires_block"
    }
}