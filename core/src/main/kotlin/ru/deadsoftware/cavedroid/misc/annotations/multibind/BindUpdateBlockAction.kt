package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.actions.updateblock.IUpdateBlockAction
import ru.deadsoftware.cavedroid.misc.annotations.multibind.MultibindingConfig
import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey

@BindsIntoMapStringKey(
    interfaceClass = IUpdateBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UpdateBlockActionsModule"
)
annotation class BindUpdateBlockAction(val stringKey: String)