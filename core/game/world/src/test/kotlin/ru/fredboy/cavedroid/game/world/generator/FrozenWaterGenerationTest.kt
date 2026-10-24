package ru.fredboy.cavedroid.game.world.generator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome

class FrozenWaterGenerationTest {
    private val items = GeneratorTestItems()
    private val config = WorldGeneratorConfig.getDefault(width = 256, seed = 42L).copy(
        minSurfaceHeight = 150,
        maxSurfaceHeight = 160,
        biomes = listOf(Biome.WINTER),
    )

    @Test
    fun `looping worlds generate frozen winter water surfaces`() {
        assertFrozenSurface(GameWorldGenerator(config, items).generate().foreMap)
    }

    @Test
    fun `streamed worlds generate frozen winter water surfaces including negative chunks`() {
        val generator = ChunkGenerator(config, items)
        for (chunkX in -1..1) {
            assertFrozenSurface(generator.generateChunk(chunkX).foreMap)
        }
    }

    @Test
    fun `both generators leave plains water liquid`() {
        val warm = config.copy(biomes = listOf(Biome.PLAINS))
        val maps = listOf(
            GameWorldGenerator(warm, items).generate().foreMap,
            ChunkGenerator(warm, items).generateChunk(0).foreMap,
        )
        for (map in maps) {
            assertFalse(map.any { column -> column.any { it.params.key == "ice" } })
            assertTrue(map.any { column -> column.any { it.isWater() } })
        }
    }

    private fun assertFrozenSurface(map: Array<Array<Block>>) {
        assertTrue(map.any { column -> column.any { it.params.key == "ice" } })
        for (column in map) {
            for (y in column.indices.filter { column[it].params.key == "ice" }) {
                assertTrue(y <= config.maxSurfaceHeight)
                assertTrue((0 until y).all { column[it].isNone() })
                assertTrue(column[y + 1].isWater())
            }
        }
    }
}
