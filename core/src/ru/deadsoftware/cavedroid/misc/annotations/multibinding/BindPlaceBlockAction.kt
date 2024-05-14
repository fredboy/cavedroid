package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.actions.placeblock.IPlaceBlockAction
import ru.fredboy.cavedroid.ksp.annotations.GenerateMapMultibindingsModule

@GenerateMapMultibindingsModule(
    interfaceClass = IPlaceBlockAction::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "PlaceBlockActionsModule"
)
annotation class BindPlaceBlockAction(val stringKey: String)
