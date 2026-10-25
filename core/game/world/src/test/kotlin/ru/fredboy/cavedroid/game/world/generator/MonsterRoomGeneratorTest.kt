package ru.fredboy.cavedroid.game.world.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block

class MonsterRoomGeneratorTest {
    private val items = GeneratorTestItems()
    private val stone = items.getBlockByKey("stone")
    private val config = WorldGeneratorConfig.getDefault(seed = 123456789L)
    private val generator = MonsterRoomGenerator(config, items)

    private fun map(width: Int, fill: Block = stone) = Array(width) { Array(config.height) { fill } }

    @Test
    fun `sealed stone generates complete rooms with centered spawners and supported chests`() {
        val fore = map(4096)
        val back = map(4096)
        generator.generate(0, fore, back)
        val mobKeys = mutableSetOf<String>()
        val widths = mutableSetOf<Int>()
        var chests = 0
        for (x in fore.indices) {
            for (y in fore[x].indices) {
                val spawner = fore[x][y] as? Block.Spawner ?: continue
                mobKeys += spawner.mobKey
                val left = (x - 1 downTo 0).first { !fore[it][y].isNone() }
                val right = (x + 1 until fore.size).first { !fore[it][y].isNone() }
                widths += right - left + 1
                assertEquals(x - left, right - x)
                assertTrue(y - 4 > config.maxSurfaceHeight && y + 1 < config.lavaLevel)
                var roomChests = 0
                for (roomX in left..right) {
                    assertEquals("cobblestone", fore[roomX][y - 4].params.key)
                    assertTrue(fore[roomX][y + 1].params.key in setOf("cobblestone", "mossy_cobblestone"))
                    if (back[roomX][y] is Block.Chest) {
                        roomChests++
                        assertTrue(roomX == left + 1 || roomX == right - 1)
                        assertTrue(fore[roomX][y].isNone())
                        assertEquals("monster_room", (back[roomX][y] as Block.Chest).lootTable)
                    }
                }
                assertTrue(roomChests in 0..2)
                chests += roomChests
            }
        }
        assertEquals(setOf("zombie", "skeleton", "spider"), mobKeys)
        assertEquals(setOf(7, 9), widths)
        assertTrue(chests > 0)
        val floors = fore.sumOf { column -> column.count { it.params.key == "mossy_cobblestone" } }
        assertTrue(floors > 0)
    }

    @Test
    fun `regions generated in reverse order match a whole world including negative coordinates`() {
        val start = -256
        val width = 512
        val fore = map(width)
        val back = map(width)
        generator.generate(start, fore, back)
        assertTrue(fore.any { column -> column.any { it is Block.Spawner } })
        for (x in (start until start + width step MonsterRoomGenerator.REGION_WIDTH).reversed()) {
            val regionFore = map(MonsterRoomGenerator.REGION_WIDTH)
            val regionBack = map(MonsterRoomGenerator.REGION_WIDTH)
            MonsterRoomGenerator(config, items).generate(x, regionFore, regionBack)
            for (localX in regionFore.indices) {
                assertTrue(fore[x - start + localX].contentEquals(regionFore[localX]), "foreground seam at $x")
                assertTrue(back[x - start + localX].contentEquals(regionBack[localX]), "background seam at $x")
            }
        }
    }

    @Test
    fun `looping world terrain includes monster rooms and mineshafts`() {
        val world = GameWorldGenerator(config, items).generate()
        val lootTables = world.backMap.flatMap { column -> column.filterIsInstance<Block.Chest>() }
            .map { it.lootTable }.toSet()
        assertEquals(setOf("mineshaft", "monster_room"), lootTables)
        assertTrue(world.foreMap.any { column -> column.any { it.params.key == "mossy_cobblestone" } })
    }

    @Test
    fun `air and fluids cannot support monster rooms`() {
        for (fill in listOf(items.fallbackBlock, items.getBlockByKey("water"), items.getBlockByKey("lava"))) {
            val fore = map(512, fill)
            val back = map(512)
            generator.generate(0, fore, back)
            assertTrue(fore.all { column -> column.all { it === fill } })
            assertTrue(back.all { column -> column.all { it === stone } })
        }
    }

    @Test
    fun `missing ceiling or floor prevents generation`() {
        val shallow = config.copy(maxSurfaceHeight = 10, lavaLevel = 25)
        for (emptyY in listOf(18, 23)) {
            val fore = map(512)
            val back = map(512)
            fore.forEach { it[emptyY] = items.fallbackBlock }
            MonsterRoomGenerator(shallow, items).generate(0, fore, back)
            assertTrue(fore.all { column -> column.all { it === stone || it === items.fallbackBlock } })
            assertTrue(back.all { column -> column.all { it === stone } })
        }
    }

    @Test
    fun `existing block entities are never overwritten in either layer`() {
        for (background in listOf(false, true)) {
            val chest = items.getBlockByKey("chest_mineshaft")
            val fore = map(512, if (background) stone else chest)
            val back = map(512, if (background) chest else stone)
            generator.generate(0, fore, back)
            assertTrue(fore.all { column -> column.all { it === if (background) stone else chest } })
            assertTrue(back.all { column -> column.all { it === if (background) chest else stone } })
        }
    }

    @Test
    fun `looping edges and shallow worlds are not cut by partial rooms`() {
        val fore = map(6)
        val back = map(6)
        generator.generate(0, fore, back, loopingWidth = 6)
        assertTrue(fore.all { column -> column.all { it === stone } })
        val shallowFore = map(512)
        val shallowBack = map(512)
        MonsterRoomGenerator(config.copy(lavaLevel = config.maxSurfaceHeight + 8), items)
            .generate(0, shallowFore, shallowBack)
        assertTrue(shallowFore.all { column -> column.all { it === stone } })
    }
}
