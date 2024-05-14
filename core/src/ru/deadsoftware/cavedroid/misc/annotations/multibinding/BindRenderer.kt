package ru.deadsoftware.cavedroid.misc.annotations.multibinding

import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.fredboy.cavedroid.ksp.annotations.GenerateSetMultibindingsModule

@GenerateSetMultibindingsModule(
    interfaceClass = IGameRenderer::class,
    modulePackage = MultibindingConfig.GENERATED_MODULES_PACKAGE,
    moduleName = "RenderModule"
)
annotation class BindRenderer
