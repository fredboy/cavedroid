package ru.fredboy.cavedroid.entity.container.model

import kotlin.random.Random

object MineshaftLoot {
    fun roll(seed: Long): List<Pair<String, Int>> {
        val random = Random(seed)
        return buildList {
            add("torch" to random.nextInt(6, 17))
            add("bread" to random.nextInt(2, 6))
            add("iron_ingot" to random.nextInt(2, 7))
            repeat(random.nextInt(2, 5)) {
                add(
                    when (random.nextInt(10)) {
                        in 0..2 -> "coal" to random.nextInt(4, 13)
                        in 3..4 -> "string" to random.nextInt(2, 7)
                        in 5..6 -> "gold_ingot" to random.nextInt(1, 4)
                        7 -> "iron_pickaxe" to 1
                        8 -> "diamond" to 1
                        else -> "planks_oak" to random.nextInt(8, 17)
                    },
                )
            }
        }
    }
}
