package ru.fredboy.cavedroid.ux.controls.action.annotation

import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.ux.controls.action.placeblock.IPlaceBlockAction

@BindsIntoMapStringKey(
    interfaceClass = IPlaceBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "PlaceBlockActionsModule"
)
annotation class BindPlaceBlockAction(val stringKey: String)
