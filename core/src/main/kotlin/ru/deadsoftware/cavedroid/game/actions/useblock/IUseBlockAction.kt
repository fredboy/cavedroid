package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.model.block.Block

interface IUseBlockAction {

    fun perform(block: Block, x: Int, y: Int)

}