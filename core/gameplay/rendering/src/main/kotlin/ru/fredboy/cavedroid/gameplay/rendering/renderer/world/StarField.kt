package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import ru.fredboy.cavedroid.common.utils.meters
import kotlin.random.Random

internal class StarField(
    seed: Long,
) {

    // Generate once, with a fixed seed, so the sky stays still between frames and sessions.
    val stars: List<Star> = Random(seed).let { random ->
        List(160) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                size = (if (random.nextFloat() < 0.15f) 2 else 1).meters,
                brightness = 0.4f + random.nextFloat() * 0.6f,
            )
        }
    }

    data class Star(val x: Float, val y: Float, val size: Float, val brightness: Float)

    companion object {
        fun opacity(sunlight: Float): Float = ((0.5f - sunlight) * 2f).coerceIn(0f, 1f)

        fun overlapsMoon(x: Float, y: Float, size: Float, moonX: Float, moonY: Float, moonRadius: Float): Boolean {
            val dx = moonX - moonX.coerceIn(x, x + size)
            val dy = moonY - moonY.coerceIn(y, y + size)
            return dx * dx + dy * dy <= moonRadius * moonRadius
        }
    }
}
