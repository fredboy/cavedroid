package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.domain.world.model.Layer

data class FireEntry(
    val x: Int,
    val y: Int,
    val layer: Layer,
    val age: Float,
)
