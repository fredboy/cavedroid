package ru.fredboy.cavedroid.gameplay.controls.input.annotation

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.gameplay.controls.input.IKeyboardInputHandler

@BindsIntoSet(
    interfaceClass = IKeyboardInputHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "KeyboardInputHandlersModule",
)
annotation class BindKeyboardInputHandler
