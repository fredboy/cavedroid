package ru.fredboy.cavedroid.game.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.world.generator.GeneratorTestItems

class WaterFreezingTest {
    private val items = GeneratorTestItems()
    private val water = items.getBlockByKey("water") as Block.Water
    private val ice = items.getBlockByKey("ice")
    private val column = Array<Block>(256) { items.fallbackBlock }

    private fun canFreeze(y: Int, biome: Biome = Biome.WINTER) = WaterFreezing.canFreeze(y, biome, 160) { column[it] }

    @Test
    fun `source water freezes up to the lowest configured surface point`() {
        for (y in listOf(0, 64, 128, 160)) {
            column.fill(items.fallbackBlock)
            column[y] = water
            assertTrue(canFreeze(y), "Water at y=$y must freeze")
        }
    }

    @Test
    fun `water below the surface limit stays liquid even with open sky`() {
        column[161] = water
        assertFalse(canFreeze(161))
    }

    @Test
    fun `water in warm biomes stays liquid`() {
        column[128] = water
        assertFalse(canFreeze(128, Biome.PLAINS))
        assertFalse(canFreeze(128, Biome.DESERT))
    }

    @Test
    fun `flowing water and lava do not freeze`() {
        for (state in 1..7) {
            column[128] = water.copy(state = state)
            assertFalse(canFreeze(128))
        }
        column[128] = items.getBlockByKey("lava")
        assertFalse(canFreeze(128))
    }

    @Test
    fun `shallow cave pool does not freeze under a distant roof`() {
        column[100] = items.getBlockByKey("stone")
        column[128] = water
        assertFalse(canFreeze(128))
    }

    @Test
    fun `freezing never spreads below the surface layer`() {
        column[128] = water
        column[129] = water
        assertFalse(canFreeze(129))

        repeat(2) { WaterFreezing.freezeSurface(column, Biome.WINTER, 160, ice) }

        assertEquals(ice, column[128])
        assertEquals(water, column[129])
        assertFalse(canFreeze(129))
    }

    @Test
    fun `empty column remains empty`() {
        WaterFreezing.freezeSurface(column, Biome.WINTER, 160, ice)
        assertTrue(column.all { it.isNone() })
    }
}
