package ru.fredboy.cavedroid.gameplay.rendering.utils

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.exp
import kotlin.math.floor

/** Angles are clockwise turns in screen coordinates, where Y points down. */
internal object InstrumentDial {
    fun wrap(turns: Float): Float = turns - floor(turns)

    // Frame 00 is noon; CaveDroid's day begins at sunrise.
    fun clock(time: Float, dayDuration: Float): Float = wrap(time / dayDuration - 0.25f)

    fun compass(playerX: Float, playerY: Float, spawnX: Float, spawnY: Float, worldWidth: Float = 0f): Float {
        var dx = spawnX - playerX
        if (worldWidth > 0f) dx -= floor(dx / worldWidth + 0.5f) * worldWidth
        val dy = spawnY - playerY
        // Frame 00 points down, 08 left, 16 up, and 24 right.
        return wrap((atan2(dy, dx) / (2 * PI)).toFloat() - 0.25f)
    }

    fun frame(turns: Float, count: Int): Int = floor(wrap(turns) * count + 0.5f).toInt().mod(count)
}

/** Follow the shortest arc, including when the angle crosses frame zero. */
internal class InstrumentNeedle {
    private var turns: Float? = null

    fun update(target: Float, delta: Float): Float {
        val previous = turns
        val next = if (previous == null || delta <= 0f) {
            target
        } else {
            val difference = InstrumentDial.wrap(target - previous + 0.5f) - 0.5f
            previous + difference * (1f - exp(-12f * delta))
        }
        return InstrumentDial.wrap(next).also { turns = it }
    }
}
