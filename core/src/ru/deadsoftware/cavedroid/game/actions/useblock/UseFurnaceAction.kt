package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@UseBlockAction(stringKey = UseFurnaceAction.KEY)
class UseFurnaceAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val gameWindowsManager: GameWindowsManager,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        val furnace = gameWorld.getForegroundFurnace(x, y) ?: gameWorld.getBackgroundFurnace(x, y) ?: return
        gameWindowsManager.openFurnace(furnace)
    }

    companion object {
        const val KEY = "furnace"
    }
}