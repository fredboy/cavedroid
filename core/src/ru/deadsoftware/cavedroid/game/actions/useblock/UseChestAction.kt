package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.objects.container.Chest
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@UseBlockAction(stringKey = UseChestAction.KEY)
class UseChestAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val gameWindowsManager: GameWindowsManager,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        val chest = (gameWorld.getForegroundContainer(x, y) as? Chest)
            ?: (gameWorld.getBackgroundContainer(x, y) as? Chest)
            ?: return
        gameWindowsManager.openChest(chest)
    }

    companion object {
        const val KEY = "chest"
    }
}