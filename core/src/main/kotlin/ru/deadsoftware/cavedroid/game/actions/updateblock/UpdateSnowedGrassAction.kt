package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateSnowedGrassAction.BLOCK_KEY)
class UpdateSnowedGrassAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mGameItemsHolder: GameItemsHolder,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val blockOnTop = gameWorld.getForeMap(x, y - 1)
        val makesDirt = blockOnTop.params.hasCollision || blockOnTop.isFluid()

        when {
            makesDirt -> gameWorld.setForeMap(x, y, mGameItemsHolder.getBlock("dirt"))
            blockOnTop.params.key != "snow" -> gameWorld.setForeMap(x, y, mGameItemsHolder.getBlock("grass"))
        }
    }

    companion object {
        const val BLOCK_KEY = "grass_snowed"
    }
}