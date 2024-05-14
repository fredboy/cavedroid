package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.fredboy.cavedroid.ksp.annotations.GenerateSetMultibindingsModule

@GenerateSetMultibindingsModule(
    interfaceClass = IKeyboardInputHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "KeyboardInputHandlersModule"
)
annotation class BindKeyboardInputHandler