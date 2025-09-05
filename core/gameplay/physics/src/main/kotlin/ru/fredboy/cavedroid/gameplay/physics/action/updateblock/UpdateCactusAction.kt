package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateCactusAction.BLOCK_KEY)
class UpdateCactusAction @Inject constructor(
    private val gameWorld: GameWorld,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (gameWorld.getForeMap(x - 1, y).params.hasCollision ||
            gameWorld.getForeMap(x + 1, y).params.hasCollision ||
            gameWorld.getForeMap(x, y + 1).params.key.let { key -> key != "cactus" && key != "sand" }
        ) {
            gameWorld.destroyForeMap(x, y, true)
        }
    }

    companion object {
        const val BLOCK_KEY = "cactus"
    }
}
