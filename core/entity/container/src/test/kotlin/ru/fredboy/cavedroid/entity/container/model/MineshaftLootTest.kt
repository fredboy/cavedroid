package ru.fredboy.cavedroid.entity.container.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MineshaftLootTest {

    @Test
    fun `both seeds are obtainable in stacks of two to four with deterministic loot`() {
        val found = mutableSetOf<String>()
        for (seed in 0L..100L) {
            val loot = MineshaftLoot.roll(seed)
            assertEquals(loot, MineshaftLoot.roll(seed))
            for ((key, count) in loot.filter { it.first.startsWith("seeds_") }) {
                found += key
                assertTrue(count in 2..4)
            }
        }
        assertEquals(setOf("seeds_pumpkin", "seeds_melon"), found)
    }
}
