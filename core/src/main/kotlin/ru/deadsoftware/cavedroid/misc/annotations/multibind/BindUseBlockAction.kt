package ru.deadsoftware.cavedroid.misc.annotations.multibind

import ru.deadsoftware.cavedroid.game.actions.useblock.IUseBlockAction
import ru.deadsoftware.cavedroid.misc.annotations.multibind.MultibindingConfig
import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey

@BindsIntoMapStringKey(
    interfaceClass = IUseBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UseBlockActionsModule"
)
annotation class BindUseBlockAction(val stringKey: String)
