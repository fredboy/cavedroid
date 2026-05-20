package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateFarmlandMoistAction.BLOCK_KEY)
class UpdateFarmlandMoistAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (!isWaterNearby(gameWorld, x, y)) {
            gameWorld.setForeMap(x, y, getBlockByKeyUseCase["farmland"])
        }
    }

    companion object {
        const val BLOCK_KEY = "farmland_moist"
    }
}
