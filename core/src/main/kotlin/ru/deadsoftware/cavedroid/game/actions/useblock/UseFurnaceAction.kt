package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindUseBlockAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseFurnaceAction.KEY)
class UseFurnaceAction @Inject constructor(
    private val containerController: ContainerController,
    private val gameWindowsManager: GameWindowsManager,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        // TODO: transform x
        val furnace = (containerController.getContainer(x, y, Layer.FOREGROUND.z)
            ?: containerController.getContainer(x, y, Layer.FOREGROUND.z)) as? Furnace
        furnace ?: return

        gameWindowsManager.openFurnace(furnace)
    }

    companion object {
        const val KEY = "furnace"
    }
}