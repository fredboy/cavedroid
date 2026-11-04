package ru.fredboy.cavedroid.gameplay.physics.action.growblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.WaterFreezing
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = FreezeWaterAction.BLOCK_KEY)
class FreezeWaterAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {

    override fun grow(x: Int, y: Int): Boolean {
        if (!WaterFreezing.canFreeze(
                y,
                gameWorld.getBiomeAt(x),
                gameWorld.generatorConfig.maxSurfaceHeight,
            ) { gameWorld.getForeMap(x, it) }
        ) {
            return false
        }

        gameWorld.setForeMap(x, y, getBlockByKeyUseCase["ice"])
        return true
    }

    companion object {
        const val BLOCK_KEY = "water"
    }
}
