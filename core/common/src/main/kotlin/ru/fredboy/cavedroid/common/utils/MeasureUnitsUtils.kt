package ru.fredboy.cavedroid.common.utils

import kotlin.math.ceil
import kotlin.math.floor

const val PIXELS_PER_METER = 16f

const val DEFAULT_VIEWPORT_WIDTH = 1000f

const val MIN_VIEWPORT_HEIGHT = 400f

val Float.meters get() = this / PIXELS_PER_METER

val Int.meters get() = this.toFloat().meters

val Float.pixels get() = this * PIXELS_PER_METER

val Float.floor get() = floor(this)

val Float.ceil get() = ceil(this)

val Int.scaleToViewport: Float get() = this.toFloat() / DEFAULT_VIEWPORT_WIDTH

infix fun Int.floorDiv(y: Int): Int {
    val x = this
    var r = x / y
    // if the signs are different and modulo not zero, round down
    if ((x xor y) < 0 && (r * y != x)) {
        r--
    }
    return r
}

infix fun Int.floorMod(y: Int): Int {
    val x = this
    var mod = x % y
    // if the signs are different and modulo not zero, adjust result
    if ((mod xor y) < 0 && mod != 0) {
        mod += y
    }
    return mod
}

fun Float.floorToInt(): Int = floor.toInt()
