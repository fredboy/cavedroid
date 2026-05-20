package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

internal fun isWaterNearby(gameWorld: GameWorld, x: Int, y: Int): Boolean {
    for (dx in -WATER_RANGE..WATER_RANGE) {
        for (dy in -1..1) {
            val block = gameWorld.getForeMap(x + dx, y + dy)
            if (block.isWater()) return true
        }
    }
    return false
}

private const val WATER_RANGE = 4

@GameScope
@BindUpdateBlockAction(stringKey = UpdateFarmlandAction.BLOCK_KEY)
class UpdateFarmlandAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (isWaterNearby(gameWorld, x, y)) {
            gameWorld.setForeMap(x, y, getBlockByKeyUseCase["farmland_moist"])
        }
    }

    companion object {
        const val BLOCK_KEY = "farmland"
    }
}
