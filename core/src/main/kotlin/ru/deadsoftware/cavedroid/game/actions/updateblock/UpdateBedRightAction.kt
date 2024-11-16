package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindUpdateBlockAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateBedRightAction.BLOCK_KEY)
class UpdateBedRightAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val bedLeft = getBlockByKeyUseCase["bed_l"]
        if (gameWorld.getForeMap(x - 1, y) != bedLeft) {
            gameWorld.resetForeMap(x, y)
        }
    }

    companion object {
        const val BLOCK_KEY = "bed_r"
    }
}