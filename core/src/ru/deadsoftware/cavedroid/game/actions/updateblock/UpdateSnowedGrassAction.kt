package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class UpdateSnowedGrassAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mGameItemsHolder: GameItemsHolder,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val blockOnTop = gameWorld.getForeMap(x, y - 1)
        if (blockOnTop.collision || blockOnTop.isFluid()) {
            gameWorld.setForeMap(x, y, mGameItemsHolder.getBlock("dirt"))
        }
    }

    companion object {
        const val BLOCK_KEY = "grass_snowed"
    }
}