package ru.fredboy.cavedroid.gameplay.controls.action

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.gameplay.controls.action.useblock.IUseBlockAction

fun Map<String, IUseBlockAction>.getUseBlockAction(block: Block): IUseBlockAction? {
    val key = block.params.useActionKey ?: block.params.key
    return get(key)
}
