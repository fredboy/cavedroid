package ru.deadsoftware.cavedroid.misc.annotations.multibind

import ru.deadsoftware.cavedroid.game.input.IMouseInputHandler
import ru.deadsoftware.cavedroid.misc.annotations.multibind.MultibindingConfig
import ru.fredboy.automultibind.annotations.BindsIntoSet

@BindsIntoSet(
    interfaceClass = IMouseInputHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "MouseInputHandlersModule"
)
annotation class BindMouseInputHandler