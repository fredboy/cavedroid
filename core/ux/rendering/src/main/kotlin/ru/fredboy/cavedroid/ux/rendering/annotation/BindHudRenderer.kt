package ru.fredboy.cavedroid.ux.rendering.annotation

import ru.fredboy.automultibind.annotations.BindsIntoSet
import ru.fredboy.cavedroid.common.automultibind.MultibindingConfig
import ru.fredboy.cavedroid.ux.rendering.renderer.hud.IHudRenderer

@BindsIntoSet(
    interfaceClass = IHudRenderer::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "HudRenderModule",
)
annotation class BindHudRenderer
