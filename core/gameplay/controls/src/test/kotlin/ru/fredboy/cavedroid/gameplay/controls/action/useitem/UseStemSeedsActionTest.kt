package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.usecase.PlantCropInteractor

class UseStemSeedsActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController> { every { player } returns this@UseStemSeedsActionTest.player }
    private val action = UseStemSeedsAction(mobs, PlantCropInteractor(world, repository))
    private val air = Block.None(mockk<CommonBlockParams>(relaxed = true))

    init {
        every { world.getForeMap(any(), any()) } returns air
        every { repository.getBlockByKey(any()) } answers { block(firstArg()) }
    }

    @ParameterizedTest
    @CsvSource("pumpkin,farmland", "melon,farmland_moist")
    fun `seeds plant on selected farmland and consume one seed`(fruit: String, soil: String) {
        every { world.getForeMap(5, 10) } returns block(soil)

        assertTrue(action.perform(seeds(fruit), 5, 10))

        val planted = slot<Block>()
        verify { world.setForeMap(5, 9, capture(planted)) }
        assertEquals("${fruit}_stem_stage0", planted.captured.params.key)
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
    }

    @ParameterizedTest
    @CsvSource("pumpkin,farmland_moist", "melon,farmland")
    fun `seeds plant in selected air above farmland`(fruit: String, soil: String) {
        every { world.getForeMap(5, 11) } returns block(soil)

        assertTrue(action.perform(seeds(fruit), 5, 10))

        val planted = slot<Block>()
        verify { world.setForeMap(5, 10, capture(planted)) }
        assertEquals("${fruit}_stem_stage0", planted.captured.params.key)
    }

    @ParameterizedTest
    @CsvSource("pumpkin,dirt", "melon,grass")
    fun `seeds cannot be planted on ordinary soil or replace another plant`(fruit: String, soil: String) {
        every { world.getForeMap(5, 11) } returns block(soil)
        assertFalse(action.perform(seeds(fruit), 5, 10))
        every { world.getForeMap(5, 11) } returns block("farmland")
        every { world.getForeMap(5, 10) } returns block("wheat_stage0")
        assertFalse(action.perform(seeds(fruit), 5, 10))

        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    private fun seeds(fruit: String): Item.Usable = mockk {
        every { params.key } returns "${fruit}_seeds"
    }

    private fun block(key: String): Block.Normal = Block.Normal(
        mockk<CommonBlockParams>(relaxed = true) { every { this@mockk.key } returns key },
    )
}
