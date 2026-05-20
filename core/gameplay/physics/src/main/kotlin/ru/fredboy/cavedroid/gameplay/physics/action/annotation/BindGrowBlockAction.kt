package ru.fredboy.cavedroid.gameplay.physics.action.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction

@BindsIntoMapStringKey(
    interfaceClass = IGrowBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "GrowBlockActionsModule",
)
annotation class BindGrowBlockAction(val stringKey: String)
