package ru.fredboy.cavedroid.game.controller.explosion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block

class CreeperDaylightRulesTest {
    @Test
    fun `open sky at or above the lowest surface qualifies only during daytime`() {
        assertTrue(CreeperDaylightRules.exposed(true, 160, 160) { ExplosionTestBlocks.air })
        assertFalse(CreeperDaylightRules.exposed(false, 100, 160) { ExplosionTestBlocks.air })
        assertFalse(CreeperDaylightRules.exposed(true, 161, 160) { ExplosionTestBlocks.air })
    }

    @Test
    fun `a shallow cave roof protects creepers from surface despawning`() {
        assertFalse(
            CreeperDaylightRules.exposed(true, 100, 160) {
                if (it == 90) ExplosionTestBlocks.stone else ExplosionTestBlocks.air
            },
        )
    }

    @Test
    fun `leaves which cast no shadows do not shelter surface creepers`() {
        val leaves = Block.Normal(ExplosionTestBlocks.params("leaves_oak").copy(castsShadows = false))
        assertTrue(
            CreeperDaylightRules.exposed(true, 100, 160) {
                if (it == 90) leaves else ExplosionTestBlocks.air
            },
        )
    }

    @Test
    fun `surface exposure accumulates for ten seconds and shelter resets the timer`() {
        var elapsed = 0f
        repeat(9) { elapsed = CreeperDaylightRules.elapsed(elapsed, true, 1f) }
        assertTrue(elapsed < CreeperDaylightRules.DESPAWN_SECONDS)
        elapsed = CreeperDaylightRules.elapsed(elapsed, true, 1f)
        assertEquals(CreeperDaylightRules.DESPAWN_SECONDS, elapsed)
        assertEquals(0f, CreeperDaylightRules.elapsed(elapsed, false, 1f))
    }
}
