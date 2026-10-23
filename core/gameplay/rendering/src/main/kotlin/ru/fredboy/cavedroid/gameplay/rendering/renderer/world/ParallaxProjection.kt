package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import kotlin.math.min

internal fun parallaxScreenY(cameraY: Float, viewportHeight: Float, seaLevel: Float): Float = min(
    seaLevel - cameraY,
    viewportHeight * (1f - cameraY / seaLevel),
)
