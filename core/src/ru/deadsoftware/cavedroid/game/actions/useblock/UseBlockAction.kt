package ru.deadsoftware.cavedroid.game.actions.useblock

import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.fredboy.cavedroid.ksp.annotations.GenerateMapMultibindingsModule

@GenerateMapMultibindingsModule(
    interfaceClass = IUseBlockAction::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.actions",
    moduleName = "UseBlockActionsModule"
)
annotation class UseBlockAction(val stringKey: String)

interface IUseBlockAction {

    fun perform(block: Block, x: Int, y: Int)

}