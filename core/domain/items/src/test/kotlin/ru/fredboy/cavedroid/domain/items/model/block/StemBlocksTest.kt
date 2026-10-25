package ru.fredboy.cavedroid.domain.items.model.block

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class StemBlocksTest {

    @Test
    fun `only valid stem ages and seed items are recognized`() {
        for (fruit in listOf("pumpkin", "melon")) {
            for (age in 0..7) {
                val key = "${fruit}_stem_stage$age"
                assertEquals(key, StemBlocks.fromKey(key)?.key)
            }
            assertEquals("${fruit}_stem_stage0", StemBlocks.fromSeeds("${fruit}_seeds")?.key)
            assertNull(StemBlocks.fromKey("${fruit}_stem_stage8"))
        }
        assertNull(StemBlocks.fromKey("wheat_stage0"))
        assertNull(StemBlocks.fromKey("pumpkin"))
        assertNull(StemBlocks.fromSeeds("wheat_seeds"))
    }

    @Test
    fun `bone meal advances by its roll without going past mature age`() {
        assertEquals(2, StemBlocks.State("pumpkin", 0).advance(2).age)
        assertEquals(5, StemBlocks.State("melon", 0).advance(5).age)
        assertEquals(7, StemBlocks.State("pumpkin", 6).advance(5).age)
    }

    @Test
    fun `hydration and adjacent farmland improve the growth chance`() {
        assertEquals(13, StemBlocks.growthBound("dirt", "farmland", "dirt"))
        assertEquals(7, StemBlocks.growthBound("dirt", "farmland_moist", "dirt"))
        assertEquals(5, StemBlocks.growthBound("farmland_moist", "farmland_moist", "farmland_moist"))
    }
}
