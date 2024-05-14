package ru.deadsoftware.cavedroid.game.actions.placeblock

import ru.deadsoftware.cavedroid.game.model.item.Item

interface IPlaceBlockAction {

    fun place(placeable: Item.Placeable, x: Int, y: Int)

}
