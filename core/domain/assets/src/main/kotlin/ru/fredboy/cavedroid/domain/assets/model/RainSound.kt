package ru.fredboy.cavedroid.domain.assets.model

import com.badlogic.gdx.audio.Sound

data class RainSound(val sound: Sound, val thunder: ThunderTimeline?)

data class ThunderTimeline(val durationSeconds: Double, val onsetsSeconds: List<Double>) {
    fun flashAt(positionSeconds: Double): Float {
        if (durationSeconds <= 0.0) return 0f
        val phase = positionSeconds.mod(durationSeconds)
        // Include the previous loop so a flash at the end can fade across the seam.
        val age = onsetsSeconds.minOfOrNull {
            if (positionSeconds < it) Double.POSITIVE_INFINITY else (phase - it).mod(durationSeconds)
        } ?: return 0f
        return when {
            age < 0.08 -> 1f
            age < 0.14 -> 0.15f
            age < 0.22 -> 0.7f
            else -> ((0.5 - age) / 0.28).toFloat().coerceIn(0f, 0.7f)
        }
    }
}
