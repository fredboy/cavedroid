package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.math.MathUtils

/**
 * Converts this value in BLOCKS into pixels
 */
val Float.px get() = this * 16f

/**
 * Converts this value in BLOCKS into pixels
 */
val Int.px get() = this * 16f

/**
 * Converts this value in PIXELS into blocks
 */
val Float.bl get() = MathUtils.floor(this / 16)