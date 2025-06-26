package ru.fredboy.cavedroid.ux.rendering.annotation

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.ux.rendering.IGameRenderer

@BindsIntoSet(
    interfaceClass = IGameRenderer::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "RenderModule"
)
annotation class BindRenderer