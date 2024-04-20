package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.model.item.Item

interface IUseItemAction {

    fun perform(item: Item.Usable, x: Int, y: Int)

}