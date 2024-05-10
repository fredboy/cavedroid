package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.objects.furnace.FurnaceController
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import javax.inject.Inject

@GameScope
class UseFurnaceAction @Inject constructor(
    private val furnaceController: FurnaceController,
    private val gameWindowsManager: GameWindowsManager,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        val furnace = furnaceController.getFurnace(x, y, 0) ?: furnaceController.getFurnace(x, y, 1) ?: return
        gameWindowsManager.openFurnace(furnace)
    }

    companion object {
        const val KEY = "furnace"
    }
}