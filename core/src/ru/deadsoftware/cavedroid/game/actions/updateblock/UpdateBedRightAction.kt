package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.FallingGravel
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class UpdateBedRightAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val gameItemsHolder: GameItemsHolder,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val bedLeft = gameItemsHolder.getBlock("bed_l")
        if (gameWorld.getForeMap(x - 1, y) != bedLeft) {
            gameWorld.resetForeMap(x, y)
        }
    }

    companion object {
        const val BLOCK_KEY = "bed_r"
    }
}