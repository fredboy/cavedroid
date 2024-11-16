package ru.deadsoftware.cavedroid.misc.annotations.multibind

import ru.deadsoftware.cavedroid.game.input.IKeyboardInputHandler
import ru.fredboy.automultibind.annotations.BindsIntoSet

@BindsIntoSet(
    interfaceClass = IKeyboardInputHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "KeyboardInputHandlersModule"
)
annotation class BindKeyboardInputHandler