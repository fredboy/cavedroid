package ru.deadsoftware.cavedroid.misc.annotations.multibind

import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.deadsoftware.cavedroid.misc.annotations.multibind.MultibindingConfig
import ru.fredboy.automultibind.annotations.BindsIntoSet

@BindsIntoSet(
    interfaceClass = IGameRenderer::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "RenderModule"
)
annotation class BindRenderer
