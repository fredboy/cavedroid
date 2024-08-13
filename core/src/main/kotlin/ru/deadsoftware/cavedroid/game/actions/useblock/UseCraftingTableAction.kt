package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUseBlockAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseCraftingTableAction.KEY)
class UseCraftingTableAction @Inject constructor(
    private val gameWindowsManager: GameWindowsManager
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        gameWindowsManager.openCrafting()
    }

    companion object {
        const val KEY = "crafting_table"
    }
}