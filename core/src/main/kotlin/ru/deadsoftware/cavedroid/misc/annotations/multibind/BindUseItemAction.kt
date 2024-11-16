package ru.deadsoftware.cavedroid.misc.annotations.multibind

import ru.deadsoftware.cavedroid.game.actions.useitem.IUseItemAction
import ru.deadsoftware.cavedroid.misc.annotations.multibind.MultibindingConfig
import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey

@BindsIntoMapStringKey(
    interfaceClass = IUseItemAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UseItemActionsModule"
)
annotation class BindUseItemAction(val stringKey: String)
