package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.actions.useblock.IUseBlockAction
import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey

@BindsIntoMapStringKey(
    interfaceClass = IUseBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UseBlockActionsModule"
)
annotation class BindUseBlockAction(val stringKey: String)
