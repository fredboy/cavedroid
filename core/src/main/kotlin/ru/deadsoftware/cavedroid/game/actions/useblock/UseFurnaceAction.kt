package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUseBlockAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseFurnaceAction.KEY)
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