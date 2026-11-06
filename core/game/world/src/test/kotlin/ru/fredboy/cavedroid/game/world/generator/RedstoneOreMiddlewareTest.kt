package ru.fredboy.cavedroid.game.world.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RedstoneOreMiddlewareTest {
    private val items = GeneratorTestItems()
    private val config = WorldGeneratorConfig.getDefault(width = 256, seed = 123456789L)

    private fun stoneContext(start: Int, width: Int, loopingWidth: Int? = null) = WorldGenerationContext(
        config,
        items,
        start,
        width,
        loopingWidth,
    ).also { context ->
        context.foreMap.forEach { it.fill(items.getBlockByKey("stone")) }
        context.backMap.forEach { it.fill(items.getBlockByKey("stone")) }
    }

    private fun generate(context: WorldGenerationContext) {
        RedstoneOreMiddleware.generate(context, GenerationRandom(config.seed, RedstoneOreMiddleware.salt))
    }

    @Test
    fun `deposits replace only deep stone in both layers and preserve bedrock`() {
        val context = stoneContext(0, config.width, config.width)
        val protectedKeys = listOf("none", "dirt", "bedrock", "ore_diamond", "water", "wood_planks_oak")
        protectedKeys.forEachIndexed { x, key ->
            context.foreMap[x].fill(items.getBlockByKey(key))
            context.backMap[x].fill(items.getBlockByKey(key))
        }
        val maps = listOf(context.foreMap, context.backMap)
        val originals = maps.map { map -> map.map { it.copyOf() } }
        generate(context)
        for ((map, before) in maps.zip(originals)) {
            var deposits = 0
            map.forEachIndexed { x, column ->
                column.forEachIndexed { y, block ->
                    if (block.params.key == "ore_redstone") {
                        deposits++
                        assertTrue(y >= config.height - 32 && y < config.height - 1)
                        assertTrue(y > config.seaLevel)
                        assertEquals("stone", before[x][y].params.key)
                    } else {
                        assertEquals(before[x][y], block)
                    }
                }
            }
            protectedKeys.forEachIndexed { x, key -> assertTrue(map[x].all { it.params.key == key }) }
            assertTrue(deposits > 0)
        }
        assertTrue(context.foreMap.contentDeepEquals(context.backMap))
    }

    @Test
    fun `deposits match across band edges and negative coordinates in either request order`() {
        val whole = stoneContext(-64, 160).also(::generate)
        val starts = (-64 until 96 step 16).toList()
        for (order in listOf(starts, starts.reversed())) {
            for (start in order) {
                val part = stoneContext(start, 16).also(::generate)
                part.foreMap.indices.forEach { x ->
                    assertTrue(part.foreMap[x].contentEquals(whole.foreMap[start + x + 64]))
                    assertTrue(part.backMap[x].contentEquals(whole.backMap[start + x + 64]))
                }
            }
        }
        val looping = stoneContext(0, config.width, config.width).also(::generate)
        for (start in listOf(0, config.width - 16)) {
            val part = stoneContext(start, 16, config.width).also(::generate)
            part.foreMap.indices.forEach { x ->
                assertTrue(part.foreMap[x].contentEquals(looping.foreMap[start + x]))
                assertTrue(part.backMap[x].contentEquals(looping.backMap[start + x]))
            }
        }
    }

    @Test
    fun `default looping and infinite worlds contain redstone in both layers`() {
        val looping = GameWorldGenerator(config, items).generate()
        val chunks = ChunkGenerator(config, items)
        val infinite = (-8..8).map(chunks::generateChunk)
        val maps = listOf(
            looping.foreMap,
            looping.backMap,
            infinite.flatMap { it.foreMap.toList() }.toTypedArray(),
            infinite.flatMap { it.backMap.toList() }.toTypedArray(),
        )
        for (map in maps) {
            assertTrue(map.any { column -> column.any { it.params.key == "ore_redstone" } })
        }
    }
}
