package ru.fredboy.cavedroid.entity.container.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MonsterRoomLootTest {
    @Test
    fun `loot is reproducible and uses supported items with bounded stacks`() {
        val ranges = mapOf(
            "wheat" to 1..4, "bread" to 1..1, "coal" to 1..4,
            "seeds_melon" to 2..4, "seeds_pumpkin" to 2..4,
            "ingot_iron" to 1..4, "bucket_empty" to 1..1, "ingot_gold" to 1..4,
            "bone" to 1..8, "gunpowder" to 1..8, "rotten_flesh" to 1..8, "string" to 1..8,
        )
        val common = setOf("bone", "gunpowder", "rotten_flesh", "string")
        val seen = mutableSetOf<String>()
        for (seed in -100L..100L) {
            val loot = MonsterRoomLoot.roll(seed)
            assertEquals(loot, MonsterRoomLoot.roll(seed))
            assertTrue(loot.count { it.first in common } in 1..3)
            assertTrue(loot.count { it.first !in common } in 2..3)
            for ((key, amount) in loot) {
                assertTrue(amount in ranges.getValue(key), "invalid stack $key: $amount")
                seen += key
            }
        }
        assertEquals(ranges.keys, seen)
    }
}
