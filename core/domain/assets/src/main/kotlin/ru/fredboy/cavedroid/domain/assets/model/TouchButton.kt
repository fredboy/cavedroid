package ru.fredboy.cavedroid.domain.assets.model

import com.badlogic.gdx.math.Rectangle

data class TouchButton(
    val rectangle: Rectangle,
    val code: Int,
    val isMouse: Boolean,
)
