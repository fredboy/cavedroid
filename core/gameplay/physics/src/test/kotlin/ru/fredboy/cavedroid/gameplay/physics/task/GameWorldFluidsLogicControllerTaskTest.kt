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
    private val ice = Block.Normal(params)
    private val waters = (0..7).map { Block.Water(params, state = it, density = 1f) }
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
        every { items.getBlocksByType(Block.Water::class.java) } returns waters
        every { items.getBlocksByType(Block.Lava::class.java) } returns emptyList()
        every { items.getBlockByKey("ice") } returns ice
        every { mobs.player.mapX } returns -5
        every { mobs.player.middleMapY } returns 128
        task = GameWorldFluidsLogicControllerTask(world, mobs, items)
    }

    @AfterEach
    fun restoreApplication() {
        Gdx.app = previousApp
    }

    @Test
    fun `fluid ticks freeze only the upper source layer at negative world coordinates`() {
        blocks[-5 to 128] = waters[0]
        blocks[-5 to 129] = waters[0]

        repeat(2) { task.exec() }

        assertEquals(ice, blocks[-5 to 128])
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
    fun `fluid ticks use the biome of each water column`() {
        every { world.getBiomeAt(-4) } returns Biome.PLAINS
        blocks[-5 to 128] = waters[0]
        blocks[-4 to 128] = waters[0]

        task.exec()

        assertEquals(ice, blocks[-5 to 128])
        assertEquals(waters[0], blocks[-4 to 128])
    }
}
