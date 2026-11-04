package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

class GameWorldFluidsLogicControllerTaskTest {
    private val previousApp = Gdx.app
    private val world = mockk<GameWorld>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val items = mockk<ItemsRepository>()
    private val params = mockk<CommonBlockParams>(relaxed = true)
    private val air = Block.None(params)
    private val stone = Block.Normal(
        mockk<CommonBlockParams>(relaxed = true) {
            every { hasCollision } returns true
        },
    )
    private val cobblestone = Block.Normal(mockk<CommonBlockParams>(relaxed = true))
    private val obsidian = Block.Normal(mockk<CommonBlockParams>(relaxed = true))
    private val waters = (0..4).map { Block.Water(params, state = it, density = 1f) }
    private val lavas = (0..4).map { Block.Lava(params, state = it, density = 1f) }
    private val blocks = mutableMapOf<Pair<Int, Int>, Block>()
    private val task: GameWorldFluidsLogicControllerTask

    init {
        Gdx.app = mockk<Application>(relaxed = true)
        every { world.width } returns 16
        every { world.generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 42L)
        every { world.getBiomeAt(any()) } returns Biome.WINTER
        every { world.getForeMap(any(), any()) } answers { blocks[firstArg<Int>() to secondArg<Int>()] ?: air }
        every { world.setForeMap(any(), any(), any(), any(), any()) } answers {
            blocks[firstArg<Int>() to secondArg<Int>()] = thirdArg()
        }
        every { world.resetForeMap(any(), any()) } answers {
            blocks.remove(firstArg<Int>() to secondArg<Int>())
        }
        every { items.getBlocksByType(Block.Water::class.java) } returns waters
        every { items.getBlocksByType(Block.Lava::class.java) } returns lavas
        every { items.getBlockByKey("stone") } returns stone
        every { items.getBlockByKey("cobblestone") } returns cobblestone
        every { items.getBlockByKey("obsidian") } returns obsidian
        every { mobs.player.mapX } returns -5
        every { mobs.player.middleMapY } returns 128
        task = GameWorldFluidsLogicControllerTask(world, mobs, items)
    }

    @AfterEach
    fun restoreApplication() {
        Gdx.app = previousApp
    }

    @Test
    fun `fluid ticks leave exposed winter sources liquid until a growth tick freezes them`() {
        blocks[-5 to 128] = waters[0]
        blocks[-5 to 129] = waters[0]

        repeat(2) { task.exec() }

        assertEquals(waters[0], blocks[-5 to 128])
        assertEquals(waters[0], blocks[-5 to 129])
    }

    @Test
    fun `fluid ticks leave deep winter water liquid`() {
        every { mobs.player.middleMapY } returns 161
        blocks[-5 to 161] = waters[0]

        task.exec()

        assertEquals(waters[0], blocks[-5 to 161])
    }

    @Test
    fun `fluid ticks leave sources liquid in both winter and plains`() {
        every { world.getBiomeAt(-4) } returns Biome.PLAINS
        blocks[-5 to 128] = waters[0]
        blocks[-4 to 128] = waters[0]

        task.exec()

        assertEquals(waters[0], blocks[-5 to 128])
        assertEquals(waters[0], blocks[-4 to 128])
    }

    @Test
    fun `falling water fills a deeper flow even from a shallower flow above`() {
        prepareFlowTest()
        blocks[-5 to 126] = waters[0]
        blocks[-5 to 127] = waters[4]
        blocks[-5 to 128] = waters[2]

        task.exec()

        assertEquals(waters[1], blocks[-5 to 128])
    }

    @Test
    fun `falling lava fills a deeper flow even from a shallower flow above`() {
        prepareFlowTest()
        blocks[-5 to 126] = lavas[0]
        blocks[-5 to 127] = lavas[4]
        blocks[-5 to 128] = lavas[2]

        task.exec()

        assertEquals(lavas[1], blocks[-5 to 128])
    }

    @Test
    fun `stronger sideways flow wins over weaker flow from the other side`() {
        prepareFlowTest()
        blocks[-6 to 128] = waters[0]
        blocks[-4 to 128] = waters[2]

        task.exec()

        assertEquals(waters[2], blocks[-5 to 128])
    }

    @Test
    fun `sideways water fills a shallower existing flow`() {
        prepareFlowTest()
        blocks[-6 to 128] = waters[0]
        blocks[-5 to 128] = waters[4]

        task.exec()

        assertEquals(waters[2], blocks[-5 to 128])
    }

    @Test
    fun `falling water does not spread sideways in midair`() {
        prepareFlowTest()
        blocks[-5 to 126] = waters[0]
        blocks[-5 to 127] = waters[1]
        blocks[-5 to 128] = waters[1]

        task.exec()

        assertEquals(null, blocks[-4 to 127])
        assertEquals(waters[2], blocks[-4 to 128])
    }

    @Test
    fun `water spreads sideways when the cell below is already full`() {
        prepareFlowTest()
        blocks[-5 to 127] = waters[0]
        blocks[-5 to 128] = waters[0]

        task.exec()

        assertEquals(waters[2], blocks[-4 to 127])
    }

    @Test
    fun `two water sources refill a collected source repeatedly`() {
        prepareFlowTest()
        blocks[-6 to 128] = waters[0]
        blocks[-4 to 128] = waters[0]

        repeat(3) {
            blocks.remove(-5 to 128)
            repeat(2) { task.exec() }

            assertEquals(waters[0], blocks[-5 to 128])
        }
    }

    @Test
    fun `water source can regenerate above another water source`() {
        prepareFlowTest()
        blocks[-6 to 128] = waters[0]
        blocks[-4 to 128] = waters[0]
        blocks[-5 to 128] = waters[2]
        blocks[-5 to 129] = waters[0]

        task.exec()

        assertEquals(waters[0], blocks[-5 to 128])
    }

    @Test
    fun `two water sources do not create an unsupported source`() {
        prepareFlowTest()
        blocks.remove(-5 to 129)
        blocks[-6 to 128] = waters[0]
        blocks[-4 to 128] = waters[0]
        blocks[-5 to 128] = waters[2]

        task.exec()

        assertEquals(waters[2], blocks[-5 to 128])
        assertEquals(waters[1], blocks[-5 to 129])
    }

    @Test
    fun `one water source does not create another source`() {
        prepareFlowTest()
        blocks[-6 to 128] = waters[0]
        blocks[-5 to 128] = waters[2]

        task.exec()

        assertEquals(waters[2], blocks[-5 to 128])
    }

    @Test
    fun `lava sources do not regenerate`() {
        prepareFlowTest()
        blocks[-6 to 128] = lavas[0]
        blocks[-4 to 128] = lavas[0]

        repeat(3) { task.exec() }

        assertEquals(lavas[2], blocks[-5 to 128])
    }

    @Test
    fun `water turns a draining lava flow into cobblestone`() {
        prepareFlowTest()
        blocks[-6 to 128] = waters[0]
        blocks[-5 to 128] = lavas[4]

        task.exec()

        assertEquals(cobblestone, blocks[-5 to 128])
    }

    @Test
    fun `water turns a lava source into obsidian`() {
        prepareFlowTest()
        blocks[-6 to 128] = waters[0]
        blocks[-5 to 128] = lavas[0]

        task.exec()

        assertEquals(obsidian, blocks[-5 to 128])
    }

    @Test
    fun `falling lava turns water into stone despite queued water flow`() {
        prepareFlowTest()
        blocks[-5 to 127] = lavas[0]
        blocks[-5 to 128] = waters[2]
        blocks[-6 to 128] = waters[0]

        task.exec()

        assertEquals(stone, blocks[-5 to 128])
    }

    @Test
    fun `water flows onto lava above a full water column`() {
        prepareFlowTest()
        blocks[-6 to 127] = waters[0]
        blocks[-6 to 128] = waters[0]
        blocks[-5 to 127] = lavas[0]

        task.exec()

        assertEquals(obsidian, blocks[-5 to 127])
    }

    @Test
    fun `a different liquid above does not sustain a disconnected flow`() {
        prepareFlowTest()
        blocks[-5 to 127] = lavas[0]
        blocks[-5 to 128] = waters[4]

        task.exec()
        blocks[-5 to 128] = waters[4]
        task.exec()

        assertEquals(null, blocks[-5 to 128])
    }

    private fun prepareFlowTest() {
        every { world.getBiomeAt(any()) } returns Biome.PLAINS
        for (x in -12..2) {
            blocks[x to 129] = stone
        }
    }
}
