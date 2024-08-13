package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.fredboy.cavedroid.domain.items.model.item.Item

interface IUseItemAction {

    fun perform(item: Item.Usable, x: Int, y: Int)

}