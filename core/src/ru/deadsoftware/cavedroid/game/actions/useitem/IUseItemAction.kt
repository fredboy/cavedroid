package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.objects.Item

interface IUseItemAction {

    fun perform(item: Item, x: Int, y: Int)

}