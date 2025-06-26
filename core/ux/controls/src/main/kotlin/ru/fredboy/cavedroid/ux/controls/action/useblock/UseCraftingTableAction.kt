package ru.fredboy.cavedroid.ux.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.ux.controls.action.annotation.BindUseBlockAction
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