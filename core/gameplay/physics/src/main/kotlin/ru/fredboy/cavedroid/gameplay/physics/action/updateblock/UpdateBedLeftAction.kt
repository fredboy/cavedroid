package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateBedLeftAction.BLOCK_KEY)
class UpdateBedLeftAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val bedRight = getBlockByKeyUseCase["bed_r"]
        if (gameWorld.getForeMap(x + 1, y) != bedRight) {
            gameWorld.resetForeMap(x, y)
        }
    }

    companion object {
        const val BLOCK_KEY = "bed_l"
    }
}
