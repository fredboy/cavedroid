package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.deadsoftware.cavedroid.game.GameItems
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class UpdateGrassAction @Inject constructor(
    private val gameWorld: GameWorld,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val blockOnTop = gameWorld.getForeMapBlock(x, y - 1)
        if (blockOnTop.collision || blockOnTop.fluid) {
            gameWorld.setForeMap(x, y, GameItems.getBlockId("dirt"))
        }
    }

    companion object {
        const val BLOCK_KEY = "grass"
    }
}