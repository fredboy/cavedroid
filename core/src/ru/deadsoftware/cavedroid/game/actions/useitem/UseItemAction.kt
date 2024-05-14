package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.fredboy.cavedroid.ksp.annotations.GenerateMapMultibindingsModule

@GenerateMapMultibindingsModule(
    interfaceClass = IUseItemAction::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.actions",
    moduleName = "UseItemActionsModule"
)
annotation class UseItemAction(val stringKey: String)

interface IUseItemAction {

    fun perform(item: Item.Usable, x: Int, y: Int)

}