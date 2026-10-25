package ru.fredboy.cavedroid.entity.container.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VillageLootTest {
    @Test
    fun `household loot is deterministic bounded and includes food`() {
        val ranges = mapOf(
            "bread" to 1..3,
            "potato" to 2..5,
            "apple" to 1..3,
            "wheat_seeds" to 2..5,
            "log_oak" to 1..3,
            "sapling_oak" to 1..2,
            "iron_ingot" to 1..2,
        )
        val seen = mutableSetOf<String>()
        for (seed in 0L..100L) {
            val loot = VillageLoot.roll(seed)
            assertEquals(loot, VillageLoot.roll(seed))
            assertTrue(loot.size in 3..6)
            for ((key, amount) in loot) {
                assertTrue(amount in ranges.getValue(key))
                seen += key
            }
        }
        assertEquals(ranges.keys, seen)
    }
}
