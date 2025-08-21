package ru.fredboy.cavedroid.common.utils

import kotlin.math.floor

const val PIXELS_PER_METER = 16f

const val DEFAULT_VIEWPORT_WIDTH = 1000f

const val MIN_VIEWPORT_HEIGHT = 450f

val Float.meters get() = this / PIXELS_PER_METER

val Int.meters get() = this.toFloat().meters

val Float.floor get() = floor(this)

val Int.scaleToViewport: Float get() = this.toFloat() / DEFAULT_VIEWPORT_WIDTH
