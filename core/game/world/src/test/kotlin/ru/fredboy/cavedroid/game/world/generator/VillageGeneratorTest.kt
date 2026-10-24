package ru.fredboy.cavedroid.game.world.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome

class VillageGeneratorTest {
    private val items = GeneratorTestItems()
    private val config = WorldGeneratorConfig.getDefault(seed = 123456789L).copy(
        minSurfaceHeight = 96,
        maxSurfaceHeight = 96,
        biomes = listOf(Biome.PLAINS),
    )
    private val generator = VillageGenerator(config, items)

    private fun terrain(width: Int) = Array(width) {
        Array<Block>(config.height) { y -> items.getBlockByKey(if (y >= 96) "dirt" else "none") }
    }

    @Test
    fun `villages contain paired supported doors beds loot and irrigated crops`() {
        val fore = terrain(1024)
        val back = terrain(1024)
        generator.generate(0, fore, back, { 96 }, { Biome.PLAINS })
        var houses = 0
        var crops = 0
        for (x in fore.indices) {
            for (y in fore[x].indices) {
                when (val key = fore[x][y].params.key) {
                    "bed_l" -> {
                        houses++
                        assertEquals("bed_r", fore[x + 1][y].params.key)
                        assertEquals(Math.floorDiv(x, ChunkGenerator.CHUNK_W), Math.floorDiv(x + 1, ChunkGenerator.CHUNK_W))
                        assertEquals("cobblestone", fore[x][y + 1].params.key)
                        assertEquals("cobblestone", fore[x + 1][y + 1].params.key)
                    }
                    "door_wood_bottom_left_closed", "door_wood_bottom_right_closed" -> {
                        assertEquals(key.replace("bottom", "top"), fore[x][y - 1].params.key)
                        assertEquals("cobblestone", fore[x][y + 1].params.key)
                        assertTrue(fore[x - 1][y].isNone() && fore[x + 1][y].isNone(), "blocked entrance at $x")
                    }
                    "wheat_stage6", "carrots_stage3", "potatoes_stage3" -> {
                        crops++
                        assertEquals("farmland_moist", fore[x][y + 1].params.key)
                        assertTrue((x - 4..x + 4).any { it in fore.indices && fore[it][y + 1].isWater() })
                    }
                }
            }
        }
        assertTrue(houses >= 3)
        assertTrue(crops > 0)
        val chests = fore.sumOf { column -> column.count { it is Block.Chest && it.lootTable == "village" } }
        assertEquals(houses, chests)
    }

    @Test
    fun `whole plans match reversed chunk slices including negative regions`() {
        val start = -512
        val width = 1024
        val fore = terrain(width)
        val back = terrain(width)
        generator.generate(start, fore, back, { 96 }, { Biome.PLAINS })
        assertTrue(fore.any { column -> column.any { it is Block.Chest } })
        for (x in (start until start + width step ChunkGenerator.CHUNK_W).reversed()) {
            val partFore = terrain(ChunkGenerator.CHUNK_W)
            val partBack = terrain(ChunkGenerator.CHUNK_W)
            generator.generate(x, partFore, partBack, { 96 }, { Biome.PLAINS })
            for (local in partFore.indices) {
                assertTrue(fore[x - start + local].contentEquals(partFore[local]), "foreground at $x")
                assertTrue(back[x - start + local].contentEquals(partBack[local]), "background at $x")
            }
        }
    }

    @Test
    fun `both world generators include villages and streamed seams agree`() {
        val finite = GameWorldGenerator(config, items).generate()
        assertTrue(finite.foreMap.any { column -> column.any { it is Block.Chest && it.lootTable == "village" } })
        val infinite = ChunkGenerator(config, items)
        val chunks = (-16..16).reversed().associateWith { infinite.generateChunk(it) }
        var chests = 0
        for (chunkX in -16 until 16) {
            val band = infinite.generateBand(chunkX)
            val next = chunks.getValue(chunkX + 1)
            for (x in 0 until ChunkGenerator.OVERSCAN) {
                val bandX = ChunkGenerator.OVERSCAN + ChunkGenerator.CHUNK_W + x
                assertTrue(band.foreBand[bandX].contentEquals(next.foreMap[x]), "foreground seam at $chunkX")
                assertTrue(band.backBand[bandX].contentEquals(next.backMap[x]), "background seam at $chunkX")
            }
            chests += next.foreMap.sumOf { column -> column.count { it is Block.Chest && it.lootTable == "village" } }
        }
        assertTrue(chests > 0)
    }

    @Test
    fun `wet steep non-plains shallow and small looping sites remain untouched`() {
        val sites = listOf<Pair<(Int) -> Int, (Int) -> Biome>>(
            { _: Int -> config.seaLevel } to { Biome.PLAINS },
            { x: Int -> if (x % 2 == 0) 80 else 110 } to { Biome.PLAINS },
            { _: Int -> 96 } to { Biome.DESERT },
            { _: Int -> 96 } to { Biome.WINTER },
            { _: Int -> 5 } to { Biome.PLAINS },
        )
        for ((height, biome) in sites) {
            val fore = terrain(512)
            val back = terrain(512)
            generator.generate(0, fore, back, height, biome)
            assertTrue(fore.contentDeepEquals(terrain(512)))
            assertTrue(back.contentDeepEquals(terrain(512)))
        }
        val fore = terrain(32)
        val back = terrain(32)
        generator.generate(0, fore, back, { 96 }, { Biome.PLAINS }, loopingWidth = 32)
        assertTrue(fore.contentDeepEquals(terrain(32)))
        assertTrue(back.contentDeepEquals(terrain(32)))
    }

    @Test
    fun `legacy generation flag prevents villages in both world types`() {
        val legacy = config.copy(villagesEnabled = false)
        val finite = GameWorldGenerator(legacy, items).generate()
        val streamed = ChunkGenerator(legacy, items)
        assertTrue(finite.foreMap.none { column -> column.any { it is Block.Chest && it.lootTable == "village" } })
        for (chunk in 0..16) {
            assertTrue(streamed.generateChunk(chunk).foreMap.none { column -> column.any { it is Block.Chest && it.lootTable == "village" } })
        }
    }

    @Test
    fun `default rolling terrain has villages on dry plains with supported foundations`() {
        val normal = WorldGeneratorConfig.getDefault(seed = 123456789L)
        val terrain = ChunkGenerator(normal, items)
        val start = -4096
        val fore = Array(8192) { Array<Block>(normal.height) { items.fallbackBlock } }
        val back = Array(8192) { Array<Block>(normal.height) { items.fallbackBlock } }
        VillageGenerator(normal, items).generate(start, fore, back, terrain::surfaceHeight, terrain::biomeAt)
        var houses = 0
        for (x in fore.indices) {
            for (y in fore[x].indices) {
                if (fore[x][y].params.key != "bed_l") continue
                houses++
                assertEquals(Biome.PLAINS, terrain.biomeAt(start + x))
                assertTrue(y + 1 < normal.seaLevel)
                assertEquals("cobblestone", fore[x][y + 1].params.key)
                assertTrue(fore[x][y + 2].params.hasCollision)
                assertEquals(Math.floorDiv(start + x, ChunkGenerator.CHUNK_W), Math.floorDiv(start + x + 1, ChunkGenerator.CHUNK_W))
            }
        }
        assertTrue(houses > 0, "no villages fit the default terrain")
    }

    @Test
    fun `reported infinite village has level clear landings outside every door`() {
        val generator = ChunkGenerator(WorldGeneratorConfig.getDefault(seed = 123456789L), items)
        val chunks = (-256..-241).associateWith { generator.generateChunk(it) }
        fun block(x: Int, y: Int) = chunks.getValue(Math.floorDiv(x, ChunkGenerator.CHUNK_W))
            .foreMap[Math.floorMod(x, ChunkGenerator.CHUNK_W)][y]

        assertEquals("door_wood_bottom_left_closed", block(-4006, 111).params.key)
        var doors = 0
        for (x in -4095..-3842) {
            for (y in 1 until config.height - 1) {
                val side = when (block(x, y).params.key) {
                    "door_wood_bottom_left_closed" -> -1
                    "door_wood_bottom_right_closed" -> 1
                    else -> continue
                }
                doors++
                assertTrue(block(x + side, y).isNone(), "blocked doorway approach at ${x + side},$y")
                assertTrue(block(x + side, y - 1).isNone(), "blocked doorway headroom at ${x + side},${y - 1}")
                assertEquals("gravel", block(x + side, y + 1).params.key, "uneven doorstep at $x,$y")
            }
        }
        assertTrue(doors >= 6, "the reported region must still contain a village")
    }
}
