package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.actions.placeblock.IPlaceBlockAction
import ru.deadsoftware.cavedroid.misc.annotations.multibind.MultibindingConfig
import ru.fredboy.automultibind.annotations.BindsIntoMapStringKey

@BindsIntoMapStringKey(
    interfaceClass = IPlaceBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "PlaceBlockActionsModule"
)
annotation class BindPlaceBlockAction(val stringKey: String)
