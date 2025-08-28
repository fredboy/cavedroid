package ru.fredboy.cavedroid.gameplay.controls.action.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.gameplay.controls.action.usemob.IUseMobAction

@BindsIntoMapStringKey(
    interfaceClass = IUseMobAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UseMobActionsModule",
)
annotation class BindUseMobAction(val stringKey: String)
