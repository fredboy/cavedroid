package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateBedLeftAction.BLOCK_KEY)
class UpdateBedLeftAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val gameItemsHolder: GameItemsHolder,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val bedRight = gameItemsHolder.getBlock("bed_r")
        if (gameWorld.getForeMap(x + 1, y) != bedRight) {
            gameWorld.resetForeMap(x, y)
        }
    }

    companion object {
        const val BLOCK_KEY = "bed_l"
    }
}