package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.domain.save.model.GrowBlockEntry
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.FreezeWaterAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.GrowGrassAction

class EnvironmentalGrowthTest {
    private val previousApp = Gdx.app
    private val world = mockk<GameWorld>(relaxed = true)
    private val air = Block.None(params("none", replaceable = true))
    private val water = Block.Water(params("water"), state = 0, density = 1f)
    private val dirt = Block.Normal(params("dirt"))
    private val grass = Block.Normal(params("grass"))
    private val snowedGrass = Block.Normal(params("grass_snowed"))
    private val ice = Block.Normal(params("ice"))
    private val blocks = mutableMapOf((-5 to 5) to water, (-5 to 10) to dirt, (-4 to 10) to grass)
    private val items = mockk<ItemsRepository> {
        every { getBlockByKey("ice") } returns ice
        every { getBlockByKey("grass") } returns grass
        every { getBlockByKey("grass_snowed") } returns snowedGrass
    }
    private val freeze = FreezeWaterAction(world, GetBlockByKeyUseCase(items))
    private val spread = GrowGrassAction(world, GetBlockByKeyUseCase(items))
    private val saves = mockk<SaveDataRepository>(relaxed = true)

    init {
        Gdx.app = mockk<Application>(relaxed = true)
        every { world.start } returns -5
        every { world.end } returns -5
        every { world.height } returns 16
        every { world.isInfinite } returns false
        every { world.isChunkLoadedAt(any()) } returns true
        every { world.getBiomeAt(any()) } returns Biome.WINTER
        every { world.generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 42L)
        every { world.getForeMap(any(), any()) } answers { blocks[firstArg<Int>() to secondArg<Int>()] ?: air }
        every { world.setForeMap(any(), any(), any(), any(), any()) } answers {
            blocks[firstArg<Int>() to secondArg<Int>()] = thirdArg()
        }
    }

    @AfterEach
    fun restoreApplication() {
        Gdx.app = previousApp
    }

    @Test
    fun `existing water and dirt change over time without any chunk edits`() {
        val task = task()
        assertEquals(setOf("water", "dirt"), task.snapshot().map { it.key }.toSet())
        repeat(29) { task.exec() }
        assertEquals(water, blocks[-5 to 5])
        assertEquals(dirt, blocks[-5 to 10])
        repeat(151) { task.exec() }
        assertEquals(ice, blocks[-5 to 5])
        assertEquals(grass, blocks[-5 to 10])
        task.dispose()
    }

    @Test
    fun `failed freezing and spreading attempts retry when conditions improve`() {
        blocks[-5 to 4] = dirt
        blocks.remove(-4 to 10)
        val task = task()
        repeat(180) { task.exec() }
        assertEquals(water, blocks[-5 to 5])
        assertEquals(dirt, blocks[-5 to 10])
        blocks.remove(-5 to 4)
        blocks[-4 to 10] = snowedGrass
        repeat(180) { task.exec() }
        assertEquals(ice, blocks[-5 to 5])
        assertEquals(grass, blocks[-5 to 10])
        task.dispose()
    }

    @Test
    fun `initial scanning preserves a saved timer`() {
        val task = task(listOf(GrowBlockEntry(-5, 5, "water", 2L)))
        assertEquals(2L, task.snapshot().single { it.key == "water" }.remainingTicks)
        task.exec()
        assertEquals(water, blocks[-5 to 5])
        task.exec()
        assertEquals(ice, blocks[-5 to 5])
        task.dispose()
    }

    @Test
    fun `streamed chunks seed timers and preserve them through unload and reload`() {
        every { world.isInfinite } returns true
        val task = task()
        task.onChunkLoaded(-1)
        task.exec()
        val pending = task.snapshot().toSet()
        assertEquals(setOf("water", "dirt"), pending.map { it.key }.toSet())
        task.onChunkUnloaded(-1)
        assertTrue(task.snapshot().isEmpty())
        every { saves.loadChunkGrowBlocks(any(), any(), -1) } returns pending.toList()
        task.onChunkLoaded(-1)
        assertEquals(pending, task.snapshot().toSet())
        task.dispose()
    }

    @Test
    fun `warm or flowing water does not freeze`() {
        every { world.getBiomeAt(any()) } returns Biome.PLAINS
        assertFalse(freeze.grow(-5, 5))
        every { world.getBiomeAt(any()) } returns Biome.WINTER
        blocks[-5 to 5] = Block.Water(params("water_1"), state = 1, density = 1f)
        assertFalse(freeze.grow(-5, 5))
    }

    @Test
    fun `buried or flooded dirt does not grow grass`() {
        blocks[-5 to 9] = dirt
        assertFalse(spread.grow(-5, 10))
        blocks[-5 to 9] = water
        assertFalse(spread.grow(-5, 10))
        assertEquals(dirt, blocks[-5 to 10])
    }

    @Test
    fun `grass spreads from snowy grass and remains snowy under snow`() {
        blocks[-4 to 10] = snowedGrass
        blocks[-5 to 9] = Block.Normal(params("snow", replaceable = true))
        assertTrue(spread.grow(-5, 10))
        assertEquals(snowedGrass, blocks[-5 to 10])
    }

    private fun task(entries: List<GrowBlockEntry> = emptyList()) = GameWorldGrowBlocksControllerTask(
        world,
        mapOf("water" to freeze, "dirt" to spread),
        saves,
        mockk(relaxed = true),
        mockk(relaxed = true),
        initialEntries = entries,
    )

    private fun params(key: String, replaceable: Boolean = false): CommonBlockParams = mockk(relaxed = true) {
        every { this@mockk.key } returns key
        every { this@mockk.replaceable } returns replaceable
        every { growActionKey } returns null
    }
}
