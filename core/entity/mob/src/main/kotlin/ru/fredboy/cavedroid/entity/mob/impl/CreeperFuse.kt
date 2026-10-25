package ru.fredboy.cavedroid.entity.mob.impl

/** A broken sight line cancels an automatic fuse; explicit ignition cannot be cancelled. */
class CreeperFuse {
    var elapsedSec = 0f
        set(value) {
            field = value.coerceIn(0f, DURATION_SEC)
        }
    var forced = false
    var started = false
        private set

    val isPrimed: Boolean get() = elapsedSec > 0f || forced
    val isReady: Boolean get() = elapsedSec >= DURATION_SEC

    fun update(distance: Float, canSeePlayer: Boolean, isCreative: Boolean, delta: Float) {
        started = false
        val inRange = distance < if (isPrimed) CANCEL_DISTANCE else IGNITION_DISTANCE
        if (forced || !isCreative && canSeePlayer && inRange) {
            started = elapsedSec == 0f
            elapsedSec += delta
        } else {
            elapsedSec = 0f
        }
    }

    companion object {
        const val DURATION_SEC = 1.5f
        const val IGNITION_DISTANCE = 1.5f
        const val CANCEL_DISTANCE = 7f
        const val TRACKING_DISTANCE = 16f
        const val EXPLOSION_POWER = 3f
    }
}
