package ru.fredboy.cavedroid.ux.controls.action.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.ux.controls.action.useblock.IUseBlockAction

@BindsIntoMapStringKey(
    interfaceClass = IUseBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UseBlockActionsModule"
)
annotation class BindUseBlockAction(val stringKey: String)
