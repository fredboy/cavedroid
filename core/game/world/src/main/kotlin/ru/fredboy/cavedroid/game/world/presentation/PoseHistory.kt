package ru.fredboy.cavedroid.game.world.presentation

import kotlin.math.abs
import kotlin.math.round

/** Render-only history; no interpolation result is ever written back to a physics body. */
class PoseHistory {
    var previousX = 0f
        private set
    var previousY = 0f
        private set
    var x = 0f
        private set
    var y = 0f
        private set
    private var previousHeading = 0f
    private var heading = 0f
    private var initialized = false

    fun reset(x: Float, y: Float, heading: Float = 0f) {
        previousHeading = heading
        this.heading = heading
        previousX = x
        previousY = y
        this.x = x
        this.y = y
        initialized = true
    }

    fun beforeStep(x: Float, y: Float, heading: Float = 0f) {
        previousHeading = heading
        this.heading = heading
        if (!initialized) reset(x, y, heading)
        previousX = x
        previousY = y
        this.x = x
        this.y = y
    }

    fun afterStep(x: Float, y: Float, heading: Float = 0f) {
        this.heading = heading
        this.x = x
        this.y = y
    }

    fun matches(x: Float, y: Float, worldWidth: Float): Boolean = initialized && abs(displacement(this.x, x, worldWidth)) < 0.0001f && abs(this.y - y) < 0.0001f

    fun sampleX(alpha: Float, worldWidth: Float): Float = x - displacement(previousX, x, worldWidth) * (1f - alpha.coerceIn(0f, 1f))
    fun sampleY(alpha: Float): Float = previousY + (y - previousY) * alpha.coerceIn(0f, 1f)

    fun sampleHeading(alpha: Float): Float = heading - displacement(previousHeading, heading, 360f) * (1f - alpha.coerceIn(0f, 1f))

    companion object {
        fun displacement(from: Float, to: Float, worldWidth: Float): Float {
            val delta = to - from
            return if (worldWidth > 0f) delta - round(delta / worldWidth) * worldWidth else delta
        }
    }
}
