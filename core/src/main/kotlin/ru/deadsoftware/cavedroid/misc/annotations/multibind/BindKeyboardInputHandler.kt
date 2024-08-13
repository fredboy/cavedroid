package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.deadsoftware.cavedroid.misc.annotations.multibind.MultibindingConfig
import ru.fredboy.automultibind.annotations.BindsIntoSet

@BindsIntoSet(
    interfaceClass = IKeyboardInputHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "KeyboardInputHandlersModule"
)
annotation class BindKeyboardInputHandler