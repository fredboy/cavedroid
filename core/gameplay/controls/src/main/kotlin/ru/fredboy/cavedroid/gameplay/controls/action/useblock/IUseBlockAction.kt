package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.domain.items.model.block.Block

interface IUseBlockAction {

    /**
     * @return true if the action was handled, false to let the input handler
     *         fall through to placement / use-item.
     */
    fun perform(block: Block, x: Int, y: Int): Boolean
}
