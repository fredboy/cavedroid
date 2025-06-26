package ru.fredboy.cavedroid.ux.controls.action.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.ux.controls.action.useitem.IUseItemAction

@BindsIntoMapStringKey(
    interfaceClass = IUseItemAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UseItemActionsModule"
)
annotation class BindUseItemAction(val stringKey: String)
