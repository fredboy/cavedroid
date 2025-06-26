package ru.fredboy.cavedroid.ux.controls.action.useitem

import ru.fredboy.cavedroid.domain.items.model.item.Item

interface IUseItemAction {

    fun perform(item: Item.Usable, x: Int, y: Int)

}