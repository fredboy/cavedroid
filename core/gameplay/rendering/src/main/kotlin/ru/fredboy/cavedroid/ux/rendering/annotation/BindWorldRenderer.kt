package ru.fredboy.cavedroid.gameplay.rendering.annotation

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.gameplay.rendering.renderer.world.IWorldRenderer

@BindsIntoSet(
    interfaceClass = IWorldRenderer::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "WorldRenderModule",
)
annotation class BindWorldRenderer
