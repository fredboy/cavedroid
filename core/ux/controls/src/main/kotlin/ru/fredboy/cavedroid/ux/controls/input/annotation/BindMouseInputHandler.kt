package ru.fredboy.cavedroid.ux.controls.input.annotation

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.ux.controls.input.IMouseInputHandler

@BindsIntoSet(
    interfaceClass = IMouseInputHandler::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "MouseInputHandlersModule",
)
annotation class BindMouseInputHandler
