package ru.fredboy.cavedroid.game.world

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Weather
import ru.fredboy.cavedroid.game.world.generator.GeneratorTestItems
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import kotlin.random.Random

class SnowAccumulationTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val items = GeneratorTestItems()
    private val blocks = mutableMapOf<Pair<Int, Int>, Block>()
    private val snow = items.getBlockByKey("snow")
    private val accumulation = SnowAccumulation(world, items, Random(42))

    init {
        every { world.weather } returns Weather.RAIN
        every { world.weatherIntensity } returns 1f
        every { world.isInfinite } returns false
        every { world.start } returns 0
        every { world.end } returns 0
        every { world.height } returns 256
        every { world.generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 42L)
        every { world.getBiomeAt(any()) } returns Biome.WINTER
        every { world.getForeMap(any(), any()) } answers {
            blocks[firstArg<Int>() to secondArg<Int>()] ?: items.fallbackBlock
        }
        every { world.setForeMap(any(), any(), any(), any(), any()) } answers {
            blocks[firstArg<Int>() to secondArg<Int>()] = thirdArg()
        }
    }

    @Test
    fun `snow settles on all full surface materials including ice and leaves`() {
        for (key in listOf("grass", "dirt", "stone", "planks_oak", "leaves_oak", "ice", "glass", "snow_block")) {
            blocks.clear()
            blocks[0 to 128] = items.getBlockByKey(key)

            accumulation.update(1f)

            assertEquals(snow, blocks[0 to 127], key)
        }
    }

    @Test
    fun `snow covers the roof instead of sheltered ground`() {
        blocks[0 to 80] = items.getBlockByKey("stone")
        blocks[0 to 128] = items.getBlockByKey("grass")

        accumulation.update(1f)

        assertEquals(snow, blocks[0 to 79])
        assertEquals(null, blocks[0 to 127])
    }

    @Test
    fun `repeated snowfall keeps a single thin layer`() {
        blocks[0 to 128] = items.getBlockByKey("grass")

        repeat(10) { accumulation.update(1f) }

        assertEquals(1, blocks.values.count { it == snow })
        verify(exactly = 1) { world.setForeMap(0, 127, snow, false, false) }
    }

    @Test
    fun `clear weather and warm biomes do not accumulate snow`() {
        blocks[0 to 128] = items.getBlockByKey("grass")
        every { world.weather } returns Weather.CLEAR
        accumulation.update(1f)

        every { world.weather } returns Weather.RAIN
        for (biome in listOf(Biome.PLAINS, Biome.DESERT)) {
            every { world.getBiomeAt(0) } returns biome
            accumulation.update(1f)
        }

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `water plants and thin snow are not buried or replaced`() {
        for (key in listOf("water", "lava", "tallgrass", "snow", "torch")) {
            blocks.clear()
            blocks[0 to 127] = items.getBlockByKey(key)
            blocks[0 to 128] = items.getBlockByKey("grass")

            accumulation.update(1f)

            assertEquals(items.getBlockByKey(key), blocks[0 to 127])
        }
        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `snow does not float over a bottom slab`() {
        val stone = items.getBlockByKey("stone")
        blocks[0 to 128] = Block.Normal(stone.params.copy(collisionMargins = BlockInsets.Meters(0f, 0.5f, 0f, 0f)))

        accumulation.update(1f)

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `accumulation respects weather intensity and waits between attempts`() {
        blocks[0 to 128] = items.getBlockByKey("grass")
        every { world.weatherIntensity } returns 0.5f

        accumulation.update(1f)
        assertEquals(null, blocks[0 to 127])
        accumulation.update(1f)
        assertEquals(snow, blocks[0 to 127])
    }

    @Test
    fun `streaming worlds sample only resident chunks including negative coordinates`() {
        every { world.isInfinite } returns true
        every { world.forEachLoadedChunk(any()) } answers {
            val visit = firstArg<(Int) -> Unit>()
            visit(-2)
            visit(3)
        }
        for (x in -32..63) blocks[x to 128] = items.getBlockByKey("stone")

        accumulation.update(1f)

        val snowyColumns = blocks.filterValues { it == snow }.keys.map { it.first }
        assertEquals(2, snowyColumns.size)
        assertTrue(snowyColumns.any { it in -32..-17 })
        assertTrue(snowyColumns.any { it in 48..63 })
    }

    @Test
    fun `snow is limited to the configured surface depth and world bounds`() {
        blocks[0 to 161] = items.getBlockByKey("stone")
        accumulation.update(1f)

        blocks.clear()
        blocks[0 to 0] = items.getBlockByKey("stone")
        accumulation.update(1f)

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }
}
