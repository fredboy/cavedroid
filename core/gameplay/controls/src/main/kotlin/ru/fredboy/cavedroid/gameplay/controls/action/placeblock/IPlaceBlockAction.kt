package ru.fredboy.cavedroid.gameplay.controls.action.placeblock

import ru.fredboy.cavedroid.domain.items.model.item.Item

interface IPlaceBlockAction {

    fun place(placeable: Item.Placeable, x: Int, y: Int): Boolean
}
