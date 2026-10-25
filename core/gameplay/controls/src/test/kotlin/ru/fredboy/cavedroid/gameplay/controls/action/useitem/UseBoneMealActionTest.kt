package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class UseBoneMealActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController> {
        every { player } returns this@UseBoneMealActionTest.player
    }
    private val repository = mockk<ItemsRepository>()
    private val action = UseBoneMealAction(world, mobs, GetBlockByKeyUseCase(repository))

    @ParameterizedTest
    @CsvSource(
        "carrots_stage0, carrots_stage1",
        "carrots_stage1, carrots_stage2",
        "carrots_stage2, carrots_stage3",
        "potatoes_stage0, potatoes_stage1",
        "potatoes_stage1, potatoes_stage2",
        "potatoes_stage2, potatoes_stage3",
        "wheat_stage5, wheat_stage6",
    )
    fun `bone meal advances growing crops and consumes one item`(currentKey: String, nextKey: String) {
        val next = block(nextKey)
        every { world.getForeMap(5, 10) } returns block(currentKey)
        every { repository.getBlockByKey(nextKey) } returns next

        assertTrue(action.perform(mockk(), 5, 10))

        verify(exactly = 1) { world.setForeMap(5, 10, next) }
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["carrots_stage3", "potatoes_stage3", "wheat_stage6"])
    fun `bone meal is not consumed on mature crops`(key: String) {
        every { world.getForeMap(5, 10) } returns block(key)

        assertFalse(action.perform(mockk(), 5, 10))

        verify(exactly = 0) { world.setForeMap(any(), any(), any()) }
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["pumpkin", "melon"])
    fun `bone meal advances stems two to five stages without making fruit`(fruit: String) {
        every { world.getForeMap(5, 10) } returns block("${fruit}_stem_stage0")
        every { world.getForeMap(5, 11) } returns block("farmland")
        every { repository.getBlockByKey(any()) } answers { block(firstArg()) }

        assertTrue(action.perform(mockk(), 5, 10))

        val grown = slot<Block>()
        verify(exactly = 1) { world.setForeMap(5, 10, capture(grown)) }
        assertTrue(grown.captured.params.key in (2..5).map { age -> "${fruit}_stem_stage$age" })
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
        verify(exactly = 0) { repository.getBlockByKey(fruit) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["pumpkin", "melon"])
    fun `bone meal stops at mature stem and is not consumed again`(fruit: String) {
        every { world.getForeMap(5, 10) } returns block("${fruit}_stem_stage6")
        every { world.getForeMap(5, 11) } returns block("farmland_moist")
        val mature = block("${fruit}_stem_stage7")
        every { repository.getBlockByKey("${fruit}_stem_stage7") } returns mature

        assertTrue(action.perform(mockk(), 5, 10))
        every { world.getForeMap(5, 10) } returns mature
        assertFalse(action.perform(mockk(), 5, 10))

        verify(exactly = 1) { world.setForeMap(5, 10, mature) }
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["pumpkin_stem_stage0", "melon_stem_stage0"])
    fun `bone meal does not grow unsupported stems`(key: String) {
        every { world.getForeMap(5, 10) } returns block(key)
        every { world.getForeMap(5, 11) } returns block("dirt")

        assertFalse(action.perform(mockk(), 5, 10))

        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
    }

    private fun block(key: String): Block.Normal {
        val params = mockk<CommonBlockParams>(relaxed = true)
        every { params.key } returns key
        return Block.Normal(params)
    }
}
