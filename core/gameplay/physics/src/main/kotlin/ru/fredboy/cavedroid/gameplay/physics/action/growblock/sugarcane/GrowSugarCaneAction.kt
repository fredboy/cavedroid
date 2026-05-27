package ru.fredboy.cavedroid.gameplay.physics.action.growblock.sugarcane

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = GrowSugarCaneAction.BLOCK_KEY)
class GrowSugarCaneAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {

    override fun grow(x: Int, y: Int): Boolean {
        if (gameWorld.getForeMap(x, y).params.key != BLOCK_KEY) return false

        // Only the topmost cane in a stack attempts to grow; lower canes stop ticking.
        if (gameWorld.getForeMap(x, y - 1).params.key == BLOCK_KEY) return false

        // Walk down to find the soil block and count the current stack height.
        var stackHeight = 1
        var groundY = y + 1
        while (groundY < gameWorld.height && gameWorld.getForeMap(x, groundY).params.key == BLOCK_KEY) {
            stackHeight++
            groundY++
        }

        if (stackHeight >= MAX_HEIGHT) return false
        if (groundY >= gameWorld.height) return false

        val ground = gameWorld.getForeMap(x, groundY)
        if (ground.params.key !in SUPPORT_KEYS) return false
        if (!isWaterAdjacent(x, groundY)) return false
        if (!gameWorld.getForeMap(x, y - 1).isNone()) return false

        gameWorld.setForeMap(x, y - 1, getBlockByKeyUseCase[BLOCK_KEY])
        return false
    }

    private fun isWaterAdjacent(x: Int, y: Int): Boolean = gameWorld.getForeMap(x - 1, y).isWater() ||
            gameWorld.getForeMap(x + 1, y).isWater()

    companion object {
        const val BLOCK_KEY = "sugar_cane"
        private const val MAX_HEIGHT = 3
        private val SUPPORT_KEYS = setOf("dirt", "grass", "grass_snowed", "sand")
    }
}
