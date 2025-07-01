package ru.fredboy.cavedroid.ux.physics.action.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.ux.physics.action.updateblock.IUpdateBlockAction

@BindsIntoMapStringKey(
    interfaceClass = IUpdateBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "UpdateBlockActionsModule",
)
annotation class BindUpdateBlockAction(val stringKey: String)
