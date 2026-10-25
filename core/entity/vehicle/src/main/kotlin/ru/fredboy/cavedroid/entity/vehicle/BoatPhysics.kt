package ru.fredboy.cavedroid.entity.vehicle

import com.badlogic.gdx.math.Rectangle
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/** Forces use submerged area, including the actual height of flowing water. Y points down. */
object BoatPhysics {
    /** A small upward kick; repeated hits cannot build up launch speed. */
    fun hitVerticalVelocity(velocity: Float): Float = velocity - (velocity + BoatTuning.HIT_BOB_SPEED).coerceIn(0f, BoatTuning.HIT_BOB_SPEED)

    fun overlapArea(a: Rectangle, b: Rectangle): Float = max(0f, min(a.x + a.width, b.x + b.width) - max(a.x, b.x)) *
        max(0f, min(a.y + a.height, b.y + b.height) - max(a.y, b.y))

    fun verticalAcceleration(submerged: Float, velocity: Float, gravity: Float): Float {
        val fraction = submerged.coerceIn(0f, 1f)
        if (fraction == 0f) return 0f
        return -gravity * fraction / BoatTuning.DRAFT - velocity * BoatTuning.VERTICAL_DAMPING * fraction
    }

    fun horizontalVelocity(velocity: Float, input: Float, submerged: Float, delta: Float): Float {
        if (submerged <= 0f) return velocity
        val coast = velocity * exp(-BoatTuning.WATER_DRAG * delta)
        val rowing = input.coerceIn(-1f, 1f) * BoatTuning.ROW_ACCELERATION * delta
        // Rowing cannot accelerate past the cap; external impulses can still coast down naturally.
        return if (rowing > 0f && coast < BoatTuning.MAX_SPEED) {
            min(coast + rowing, BoatTuning.MAX_SPEED)
        } else if (rowing < 0f && coast > -BoatTuning.MAX_SPEED) {
            max(coast + rowing, -BoatTuning.MAX_SPEED)
        } else {
            coast
        }
    }
}
