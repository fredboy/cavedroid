package ru.fredboy.cavedroid.game.world.generator

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

class MineshaftGeneratorTest {
    private val config = WorldGeneratorConfig.getDefault(seed = 123456789L)
    private val blocks = mutableMapOf<String, Block>()
    private val air = Block.None(params("none"))
    private val stone = block("stone")
    private val items = mockk<ItemsRepository>().also {
        every { it.fallbackBlock } returns air
        every { it.getBlockByKey(any()) } answers { block(firstArg()) }
    }
    private val generator = MineshaftGenerator(config, items)

    private fun block(key: String): Block = blocks.getOrPut(key) {
        Block.Normal(params(key))
    }

    private fun params(key: String) = CommonBlockParams(
        key = key, collisionMargins = BlockInsets.Meters(0f, 0f, 0f, 0f),
        hitPoints = 1, dropInfo = emptyList(), dropsSingleItem = true,
        hasCollision = true, isBackground = false, isTransparent = false,
        requiresBlock = false, allowAttachToNeighbour = false, animationInfo = null,
        texture = null, spriteMargins = BlockInsets.Pixels(0, 0, 0, 0), toolLevel = 0,
        toolType = null, damage = 0, tint = null, isFallable = false, castsShadows = true,
        lightInfo = null, replaceable = false, material = null, actionSoundKey = null,
        combustible = false, requiresBackground = false,
    )

    private fun map(width: Int) = Array(width) { Array<Block>(config.height) { stone } }

    @AfterEach
    fun clearRecordedCalls() {
        clearAllMocks()
    }

    @Test
    fun `independent chunks match a whole region including negative coordinates`() {
        val start = -384
        val width = 768
        val fore = map(width)
        val back = map(width)
        generator.generate(start, fore, back)
        for (x in (start until start + width step ChunkGenerator.CHUNK_W).reversed()) {
            val chunkFore = map(ChunkGenerator.CHUNK_W)
            val chunkBack = map(ChunkGenerator.CHUNK_W)
            MineshaftGenerator(config, items).generate(x, chunkFore, chunkBack)
            for (localX in chunkFore.indices) {
                assertTrue(fore[x - start + localX].contentEquals(chunkFore[localX]), "foreground seam at $x")
                assertTrue(back[x - start + localX].contentEquals(chunkBack[localX]), "background seam at $x")
            }
        }
    }

    @Test
    fun `mines contain reachable loot supports webs and spawners below sea and above lava`() {
        val fore = map(1024)
        val back = map(1024)
        generator.generate(0, fore, back, loopingWidth = 1024)
        val counts = mutableMapOf<String, Int>()
        for (x in fore.indices) {
            for (y in 0 until config.height) {
                for (block in listOf(fore[x][y], back[x][y])) {
                    if (block === stone) continue
                    assertTrue(y > config.seaLevel && y < config.lavaLevel)
                    if (block !== air) counts.merge(block.params.key, 1, Int::plus)
                }
                if (back[x][y] == block("chest_mineshaft")) {
                    assertEquals(air, fore[x][y])
                    assertEquals(block("planks_oak"), fore[x][y + 1])
                }
            }
        }
        for (key in listOf("chest_mineshaft", "spawner", "fence_oak", "web", "ladder")) {
            assertTrue((counts[key] ?: 0) > 0, "missing $key")
        }
        assertEquals(counts.getValue("spawner") * 2, counts.getValue("chest_mineshaft"))
    }

    @Test
    fun `torches are attached to supports and leave spider nests dark`() {
        val fore = map(1024)
        val back = map(1024)
        generator.generate(0, fore, back, loopingWidth = 1024)
        val spawners = fore.indices.filter { x -> fore[x].any { it == block("spawner") } }
        var torches = 0
        for (x in fore.indices) {
            for (y in fore[x].indices) {
                if (fore[x][y] != block("torch")) continue
                torches++
                assertEquals(block("fence_oak"), back[x][y])
                assertTrue(spawners.all { kotlin.math.abs(it - x) > 13 })
            }
        }
        assertTrue(torches > 0)
    }

    @Test
    fun `shallow worlds and small looping worlds are left intact`() {
        val fore = map(32)
        val back = map(32)
        generator.generate(0, fore, back, loopingWidth = 32)
        assertTrue(fore.all { column -> column.all { it === stone } })
        val shallow = config.copy(lavaLevel = config.seaLevel + 8)
        MineshaftGenerator(shallow, items).generate(0, fore, back)
        assertTrue(fore.all { column -> column.all { it === stone } })
    }
}
