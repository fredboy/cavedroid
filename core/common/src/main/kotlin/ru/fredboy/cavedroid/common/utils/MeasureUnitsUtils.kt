package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.math.MathUtils

const val BLOCK_SIZE_PX = 16f

/**
 * Converts this value in BLOCKS into pixels
 */
val Float.px get() = this * BLOCK_SIZE_PX

/**
 * Converts this value in BLOCKS into pixels
 */
val Int.px get() = this * BLOCK_SIZE_PX

/**
 * Converts this value in PIXELS into blocks
 */
val Float.bl get() = MathUtils.floor(this / BLOCK_SIZE_PX)

/**
 * Converts this value in PIXELS into blocks
 */
val Int.bl get() = MathUtils.floor(this / BLOCK_SIZE_PX)
