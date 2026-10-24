package ru.fredboy.cavedroid.gameplay.physics.action.growblock

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import kotlin.random.Random

class GrowStemActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val action = GrowStemAction(world, GetBlockByKeyUseCase(repository))
    private val cells = mutableMapOf<Pair<Int, Int>, Block>()
    private val air = Block.None(mockk<CommonBlockParams>(relaxed = true))
    private val random = mockk<Random> {
        every { nextInt(any()) } returns 0
        every { nextBoolean() } returns true
    }

    init {
        every { world.isChunkLoadedAt(any()) } returns true
        every { world.getLightAt(any(), any()) } returns 1f
        every { world.getForeMap(any(), any()) } answers { cells[firstArg<Int>() to secondArg<Int>()] ?: air }
        every { repository.getBlockByKey(any()) } answers { block(firstArg()) }
        every { world.setForeMap(any(), any(), any(), any(), any()) } answers {
            cells[firstArg<Int>() to secondArg<Int>()] = thirdArg()
        }
        cells[5 to 11] = block("farmland_moist")
        cells[6 to 11] = block("farmland_moist")
        cells[4 to 11] = block("dirt")
    }

    @ParameterizedTest
    @ValueSource(strings = ["pumpkin", "melon"])
    fun `stem advances through all eight ages then produces its fruit`(fruit: String) {
        cells[5 to 10] = block("${fruit}_stem_stage0")
        repeat(7) { assertTrue(action.grow(5, 10, random)) }
        assertKey(5, 10, "${fruit}_stem_stage7")

        assertFalse(action.grow(5, 10, random))

        assertKey(6, 10, fruit)
        assertKey(6, 11, "dirt")
        assertKey(5, 10, "${fruit}_stem_stage7")
    }

    @ParameterizedTest
    @ValueSource(strings = ["pumpkin", "melon"])
    fun `harvesting fruit allows the mature stem to produce again`(fruit: String) {
        cells[5 to 10] = block("${fruit}_stem_stage7")
        action.grow(5, 10, random)
        cells.remove(6 to 10)

        assertFalse(action.grow(5, 10, random))

        assertKey(6, 10, fruit)
        verify(exactly = 2) { world.setForeMap(6, 10, any(), any(), any()) }
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 1])
    fun `a fruit on either side prevents additional fruit`(side: Int) {
        cells[5 to 10] = block("pumpkin_stem_stage7")
        cells[5 + side to 10] = block("pumpkin")

        assertFalse(action.grow(5, 10, random))

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `a blocked chosen side does not fall back to the free side`() {
        cells[5 to 10] = block("melon_stem_stage7")
        cells[6 to 10] = block("tallgrass")

        assertFalse(action.grow(5, 10, random))

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `fruit can grow to the left on grass`() {
        cells[5 to 10] = block("melon_stem_stage7")
        cells[4 to 11] = block("grass")
        every { random.nextBoolean() } returns false

        action.grow(5, 10, random)

        assertKey(4, 10, "melon")
        assertKey(4, 11, "grass")
    }

    @Test
    fun `fruit cannot grow on stone`() {
        cells[5 to 10] = block("melon_stem_stage7")
        cells[6 to 11] = block("stone")

        assertFalse(action.grow(5, 10, random))

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @ParameterizedTest
    @ValueSource(floats = [0f, 0.53f, 0.599f])
    fun `stems do not grow below light level nine`(light: Float) {
        cells[5 to 10] = block("pumpkin_stem_stage0")
        every { world.getLightAt(5, 10) } returns light

        assertFalse(action.grow(5, 10, random))

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `stems can grow at light level nine on dry farmland`() {
        cells[5 to 10] = block("pumpkin_stem_stage0")
        cells[5 to 11] = block("farmland")
        every { world.getLightAt(5, 10) } returns 9f / 15f

        assertTrue(action.grow(5, 10, random))
    }

    @Test
    fun `invalid soil and unloaded neighbors prevent growth`() {
        cells[5 to 10] = block("melon_stem_stage0")
        cells[5 to 11] = block("dirt")
        assertFalse(action.grow(5, 10, random))
        cells[5 to 11] = block("farmland")
        every { world.isChunkLoadedAt(6) } returns false
        assertFalse(action.grow(5, 10, random))

        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `failed growth rolls leave the stem unchanged`() {
        cells[5 to 10] = block("melon_stem_stage0")
        every { random.nextInt(any()) } returns 1

        assertFalse(action.grow(5, 10, random))

        assertKey(5, 10, "melon_stem_stage0")
    }

    private fun assertKey(x: Int, y: Int, key: String) {
        assertEquals(key, cells[x to y]?.params?.key)
    }

    private fun block(key: String): Block.Normal = Block.Normal(
        mockk<CommonBlockParams>(relaxed = true) { every { this@mockk.key } returns key },
    )
}
