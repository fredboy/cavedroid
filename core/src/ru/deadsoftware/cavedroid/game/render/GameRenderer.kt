package ru.deadsoftware.cavedroid.game.render

import ru.fredboy.cavedroid.ksp.annotations.GenerateSetMultibindingsModule

@GenerateSetMultibindingsModule(
    interfaceClass = IGameRenderer::class,
    modulePackage = "ru.deadsoftware.cavedroid.game.render",
    moduleName = "RenderModule"
)
annotation class GameRenderer
