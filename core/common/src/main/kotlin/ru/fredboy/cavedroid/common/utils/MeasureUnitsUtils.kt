package ru.fredboy.cavedroid.common.utils

import kotlin.math.floor

const val PIXELS_PER_METER = 16f

val Float.meters get() = this / PIXELS_PER_METER

val Int.meters get() = this.toFloat().meters

val Float.floor get() = floor(this)
