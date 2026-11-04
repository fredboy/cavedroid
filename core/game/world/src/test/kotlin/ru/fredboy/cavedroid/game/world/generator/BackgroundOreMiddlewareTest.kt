package ru.fredboy.cavedroid.game.world.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BackgroundOreMiddlewareTest {
    private val items = GeneratorTestItems()
    private val config = WorldGeneratorConfig.getDefault(width = 256, seed = 123456789L)
    private val previousPipeline = WorldGenerationPipeline(
        listOf(MineshaftMiddleware, MonsterRoomMiddleware, VillageMiddleware, SurfaceFreezingMiddleware),
    )

    private fun stoneContext(start: Int, width: Int, loopingWidth: Int? = null) = WorldGenerationContext(
        config,
        items,
        start,
        width,
        loopingWidth,
    ).also { context ->
        context.backMap.forEach { it.fill(items.getBlockByKey("stone")) }
    }

    private fun generate(context: WorldGenerationContext) {
        BackgroundOreMiddleware.generate(context, GenerationRandom(config.seed, BackgroundOreMiddleware.salt))
    }

    @Test
    fun `only background stone strictly below sea level and above bedrock receives ores`() {
        val context = stoneContext(0, config.width, config.width)
        val protectedKeys = listOf("none", "dirt", "sand", "bedrock", "wood_planks_oak", "chest_mineshaft", "cobblestone", "water")
        protectedKeys.forEachIndexed { x, key -> context.backMap[x].fill(items.getBlockByKey(key)) }
        val before = context.backMap.map { it.copyOf() }
        val found = mutableSetOf<String>()

        generate(context)

        context.backMap.forEachIndexed { x, column ->
            column.forEachIndexed { y, block ->
                if (block != before[x][y]) {
                    assertEquals("stone", before[x][y].params.key)
                    assertTrue(y > config.seaLevel && y < config.height - 1, "changed protected row $y")
                    found += block.params.key
                }
                assertTrue(context.foreMap[x][y].isNone(), "foreground changed at $x,$y")
            }
        }
        assertEquals(setOf("ore_coal", "ore_iron", "ore_gold", "ore_diamond", "ore_lapis"), found)
    }

    @Test
    fun `veins match across arbitrary band edges and negative coordinates in either request order`() {
        val whole = stoneContext(-64, 160).also(::generate)
        val starts = (-64 until 96 step 16).toList()
        for (order in listOf(starts, starts.reversed())) {
            for (start in order) {
                val part = stoneContext(start, 16).also(::generate)
                part.backMap.forEachIndexed { x, column ->
                    assertTrue(column.contentEquals(whole.backMap[start + x + 64]), "band mismatch at ${start + x}")
                }
            }
        }
    }

    @Test
    fun `looping generation clips external vein origins and handles worlds with no eligible depth`() {
        val whole = stoneContext(0, config.width, config.width).also(::generate)
        for (start in listOf(0, config.width - 16)) {
            val part = stoneContext(start, 16, config.width).also(::generate)
            part.backMap.forEachIndexed { x, column ->
                assertTrue(column.contentEquals(whole.backMap[start + x]), "looping edge mismatch at ${start + x}")
            }
        }
        val shallow = WorldGenerationContext(config.copy(seaLevel = config.height - 1), items, 0, 16)
        shallow.backMap.forEach { it.fill(items.getBlockByKey("stone")) }
        generate(shallow)
        assertTrue(shallow.backMap.all { column -> column.all { it.params.key == "stone" } })
        generate(stoneContext(0, 0))
    }

    @Test
    fun `full pipeline preserves looping and infinite foreground biomes structures and band dimensions`() {
        val pipeline = WorldGenerationPipeline.full()
        assertEquals(previousPipeline.regionAlignment, pipeline.regionAlignment)
        assertEquals(previousPipeline.horizontalReach, pipeline.horizontalReach)
        var changed = 0
        fun compare(before: GameWorldGenerator.GeneratedWorld, after: GameWorldGenerator.GeneratedWorld) {
            assertTrue(before.foreMap.contentDeepEquals(after.foreMap), "foreground changed")
            assertTrue(before.biomes.contentEquals(after.biomes), "biomes changed")
            before.backMap.forEachIndexed { x, column ->
                column.forEachIndexed { y, block ->
                    val replacement = after.backMap[x][y]
                    if (block != replacement) {
                        changed++
                        assertEquals("stone", block.params.key, "structure or other backing changed at $x,$y")
                        assertTrue(replacement.params.key.startsWith("ore_"))
                        assertTrue(y > config.seaLevel && y < config.height - 1)
                    }
                }
            }
        }
        compare(GameWorldGenerator(config, items, previousPipeline).generate(), GameWorldGenerator(config, items, pipeline).generate())
        val oldChunks = ChunkGenerator(config, items, previousPipeline)
        val newChunks = ChunkGenerator(config, items, pipeline)
        for (chunkX in -16..16) {
            val before = oldChunks.generateChunk(chunkX)
            val after = newChunks.generateChunk(chunkX)
            compare(
                GameWorldGenerator.GeneratedWorld(before.foreMap, before.backMap, before.biomes),
                GameWorldGenerator.GeneratedWorld(after.foreMap, after.backMap, after.biomes),
            )
        }
        assertTrue(changed > 0, "no background ores generated")
    }
}
