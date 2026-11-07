package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.window.waystone.WaystoneWindowController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = "waystone")
class UseWaystoneAction @Inject constructor(private val waystones: WaystoneWindowController) : IUseBlockAction {
    override fun perform(block: Block, x: Int, y: Int): Boolean {
        return waystones.open(x, y)
    }
}
