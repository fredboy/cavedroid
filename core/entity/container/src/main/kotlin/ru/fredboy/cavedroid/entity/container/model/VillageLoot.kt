package ru.fredboy.cavedroid.entity.container.model

import kotlin.random.Random

/** Small household supplies for the plains village's residential chests. */
object VillageLoot {
    fun roll(seed: Long): List<Pair<String, Int>> {
        val random = Random(seed)
        return List(random.nextInt(3, 7)) {
            when (random.nextInt(10)) {
                0, 1, 2 -> "bread" to random.nextInt(1, 4)
                3, 4 -> "potato" to random.nextInt(2, 6)
                5 -> "apple" to random.nextInt(1, 4)
                6 -> "wheat_seeds" to random.nextInt(2, 6)
                7 -> "log_oak" to random.nextInt(1, 4)
                8 -> "sapling_oak" to random.nextInt(1, 3)
                else -> "iron_ingot" to random.nextInt(1, 3)
            }
        }
    }
}
