package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUseBlockAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.controller.container.model.Chest
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseChestAction.KEY)
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