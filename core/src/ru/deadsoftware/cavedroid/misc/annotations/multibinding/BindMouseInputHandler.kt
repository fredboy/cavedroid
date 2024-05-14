package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler
import ru.fredboy.cavedroid.ksp.annotations.GenerateSetMultibindingsModule

@GenerateSetMultibindingsModule(
    interfaceClass = IMouseInputHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "MouseInputHandlersModule"
)
annotation class BindMouseInputHandler