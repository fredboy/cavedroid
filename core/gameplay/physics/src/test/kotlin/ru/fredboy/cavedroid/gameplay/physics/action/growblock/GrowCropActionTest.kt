package ru.fredboy.cavedroid.gameplay.physics.action.growblock

import io.mockk.every
import io.mockk.mockk
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
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.carrots.GrowCarrotStage0Action
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.carrots.GrowCarrotStage1Action
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.carrots.GrowCarrotStage2Action
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.potatoes.GrowPotatoStage0Action
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.potatoes.GrowPotatoStage1Action
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.potatoes.GrowPotatoStage2Action

class GrowCropActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val getBlock = GetBlockByKeyUseCase(repository)
    private val actions = mapOf(
        "carrots_stage0" to GrowCarrotStage0Action(world, getBlock),
        "carrots_stage1" to GrowCarrotStage1Action(world, getBlock),
        "carrots_stage2" to GrowCarrotStage2Action(world, getBlock),
        "potatoes_stage0" to GrowPotatoStage0Action(world, getBlock),
        "potatoes_stage1" to GrowPotatoStage1Action(world, getBlock),
        "potatoes_stage2" to GrowPotatoStage2Action(world, getBlock),
    )

    @ParameterizedTest
    @CsvSource(
        "carrots_stage0, carrots_stage1, farmland",
        "carrots_stage1, carrots_stage2, farmland_moist",
        "carrots_stage2, carrots_stage3, farmland",
        "potatoes_stage0, potatoes_stage1, farmland_moist",
        "potatoes_stage1, potatoes_stage2, farmland",
        "potatoes_stage2, potatoes_stage3, farmland_moist",
    )
    fun `crops advance one stage on farmland`(currentKey: String, nextKey: String, soilKey: String) {
        val next = block(nextKey)
        every { world.getForeMap(5, 10) } returns block(currentKey)
        every { world.getForeMap(5, 11) } returns block(soilKey)
        every { repository.getBlockByKey(nextKey) } returns next

        assertTrue(actions.getValue(currentKey).grow(5, 10))

        verify(exactly = 1) { world.setForeMap(5, 10, next) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["carrots_stage0", "potatoes_stage0"])
    fun `stale growth timers cannot replace another block`(key: String) {
        every { world.getForeMap(5, 10) } returns block("stone")

        assertFalse(actions.getValue(key).grow(5, 10))

        verify(exactly = 0) { world.setForeMap(any(), any(), any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["carrots_stage0", "potatoes_stage0"])
    fun `crops do not grow without farmland`(key: String) {
        every { world.getForeMap(5, 10) } returns block(key)
        every { world.getForeMap(5, 11) } returns block("dirt")

        assertFalse(actions.getValue(key).grow(5, 10))

        verify(exactly = 0) { world.setForeMap(any(), any(), any()) }
    }

    private fun block(key: String): Block.Normal {
        val params = mockk<CommonBlockParams>(relaxed = true)
        every { params.key } returns key
        return Block.Normal(params)
    }
}
