package ru.deadsoftware.cavedroid.game.actions.placeblock

import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.fredboy.cavedroid.ksp.annotations.GenerateMapMultibindingsModule

@GenerateMapMultibindingsModule(
    interfaceClass = IPlaceBlockAction::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.actions",
    moduleName = "PlaceBlockActionsModule"
)
annotation class PlaceBlockAction(val stringKey: String)

interface IPlaceBlockAction {

    fun place(placeable: Item.Placeable, x: Int, y: Int)

}
