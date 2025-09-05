package ru.fredboy.cavedroid.game.world.generator

import kotlin.math.floor
import kotlin.random.Random

class PerlinNoise(seed: Long) {

    private val permutation = IntArray(512)

    init {
        val p = IntArray(256) { it }
        val random = Random(seed)
        for (i in p.indices.reversed()) {
            val j = random.nextInt(i + 1)
            val tmp = p[i]
            p[i] = p[j]
            p[j] = tmp
        }
        for (i in 0 until 512) {
            permutation[i] = p[i and 255]
        }
    }

    private fun fade(t: Double): Double = t * t * t * (t * (t * 6 - 15) + 10)

    private fun lerp(t: Double, a: Double, b: Double): Double = a + t * (b - a)

    private fun grad(hash: Int, x: Double, y: Double): Double {
        val h = hash and 7
        val u = if (h < 4) x else y
        val v = if (h < 4) y else x
        return (
            (if ((h and 1) == 0) u else -u) +
                (if ((h and 2) == 0) v else -v)
            )
    }

    fun noise2D(x: Double, y: Double): Double {
        val xi = floor(x).toInt() and 255
        val yi = floor(y).toInt() and 255
        val xf = x - floor(x)
        val yf = y - floor(y)

        val u = fade(xf)
        val v = fade(yf)

        val aa = permutation[permutation[xi] + yi]
        val ab = permutation[permutation[xi] + yi + 1]
        val ba = permutation[permutation[xi + 1] + yi]
        val bb = permutation[permutation[xi + 1] + yi + 1]

        val x1 = lerp(
            u,
            grad(aa, xf, yf),
            grad(ba, xf - 1, yf),
        )
        val x2 = lerp(
            u,
            grad(ab, xf, yf - 1),
            grad(bb, xf - 1, yf - 1),
        )

        return (lerp(v, x1, x2) + 1) / 2
    }

    fun periodicFractalNoise1D(
        x: Int,
        worldWidth: Int,
        scale: Double,
        octaves: Int,
        persistence: Double = 0.5,
        lacunarity: Double = 2.0,
    ): Double {
        var total = 0.0
        var frequency = 1.0
        var amplitude = 1.0
        var maxValue = 0.0

        for (i in 0 until octaves) {
            val angle = 2.0 * Math.PI * (x.toDouble() / worldWidth)
            val nx = kotlin.math.cos(angle) * scale * frequency
            val ny = kotlin.math.sin(angle) * scale * frequency

            total += noise2D(nx, ny) * amplitude
            maxValue += amplitude

            amplitude *= persistence
            frequency *= lacunarity
        }

        return total / maxValue
    }
}
