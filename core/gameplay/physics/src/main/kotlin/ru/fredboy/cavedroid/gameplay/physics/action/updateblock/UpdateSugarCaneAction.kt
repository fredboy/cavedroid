package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateSugarCaneAction.BLOCK_KEY)
class UpdateSugarCaneAction @Inject constructor(
    private val gameWorld: GameWorld,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val below = gameWorld.getForeMap(x, y + 1)
        val belowKey = below.params.key

        if (belowKey == BLOCK_KEY) return
        if (belowKey !in SUPPORT_KEYS) {
            gameWorld.destroyForeMap(x, y, true)
            return
        }

        if (!isWaterAdjacent(x, y + 1)) {
            gameWorld.destroyForeMap(x, y, true)
        }
    }

    private fun isWaterAdjacent(x: Int, y: Int): Boolean = gameWorld.getForeMap(x - 1, y).isWater() ||
        gameWorld.getForeMap(x + 1, y).isWater()

    companion object {
        const val BLOCK_KEY = "sugar_cane"
        private val SUPPORT_KEYS = setOf("dirt", "grass", "grass_snowed", "sand")
    }
}
