package ru.fredboy.cavedroid.entity.container.model

import kotlin.random.Random

/** Minecraft's supply and common-drop pools, restricted to items supported by CaveDroid. */
object MonsterRoomLoot {
    fun roll(seed: Long): List<Pair<String, Int>> {
        val random = Random(seed)
        return buildList {
            // Preserve the relative weights and stack sizes of the supported supply entries.
            // Redstone, beetroot seeds, and the equipment/enchantment/music pool are not available.
            repeat(random.nextInt(2, 4)) {
                add(
                    when (random.nextInt(100)) {
                        in 0..19 -> "wheat" to random.nextInt(1, 5)
                        in 20..39 -> "bread" to 1
                        in 40..54 -> "coal" to random.nextInt(1, 5)
                        in 55..64 -> "melon_seeds" to random.nextInt(2, 5)
                        in 65..74 -> "pumpkin_seeds" to random.nextInt(2, 5)
                        in 75..84 -> "iron_ingot" to random.nextInt(1, 5)
                        in 85..94 -> "bucket_empty" to 1
                        else -> "gold_ingot" to random.nextInt(1, 5)
                    },
                )
            }
            repeat(random.nextInt(1, 4)) {
                val key = when (random.nextInt(4)) {
                    0 -> "bone"
                    1 -> "gunpowder"
                    2 -> "rotten_flesh"
                    else -> "string"
                }
                add(key to random.nextInt(1, 9))
            }
        }
    }
}
