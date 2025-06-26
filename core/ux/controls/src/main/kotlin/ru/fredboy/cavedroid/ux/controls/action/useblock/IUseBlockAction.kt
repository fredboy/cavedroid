package ru.fredboy.cavedroid.ux.controls.action.useblock

import ru.fredboy.cavedroid.domain.items.model.block.Block

interface IUseBlockAction {

    fun perform(block: Block, x: Int, y: Int)

}