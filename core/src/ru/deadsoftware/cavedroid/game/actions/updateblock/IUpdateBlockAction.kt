package ru.deadsoftware.cavedroid.game.actions.updateblock

import ru.fredboy.cavedroid.ksp.annotations.GenerateMapMultibindingsModule

@GenerateMapMultibindingsModule(
    interfaceClass = IUpdateBlockAction::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.actions",
    moduleName = "UpdateBlockActionsModule"
)
annotation class UpdateBlockAction(val stringKey: String)

interface IUpdateBlockAction {

    fun update(x: Int, y: Int)

}