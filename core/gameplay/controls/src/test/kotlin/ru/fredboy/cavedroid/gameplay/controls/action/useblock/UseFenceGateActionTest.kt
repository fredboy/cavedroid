package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.block.FenceBlocks
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class UseFenceGateActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val mobs = mockk<MobController>(relaxed = true)
    private val action = UseFenceGateAction(world, GetBlockByKeyUseCase(repository), mobs)
    private val open = gate(open = true)
    private val closed = gate(open = false)

    init {
        every { repository.getBlockByKey("fence_gate_oak_left_open") } returns open
        every { repository.getBlockByKey("fence_gate_oak_left_closed") } returns closed
        every { mobs.player.hitbox } returns Rectangle(10f, 10f, 1f, 2f)
        every { mobs.mobs } returns emptyList()
    }

    @Test
    fun `opening and closing swap the world state without drops`() {
        assertTrue(action.perform(closed, 0, 0))
        verify { world.setForeMap(0, 0, open, false, false) }
        assertTrue(action.perform(open, 0, 0))
        verify { world.setForeMap(0, 0, closed, false, false) }
    }

    @Test
    fun `closing cannot create a collider inside the player`() {
        every { mobs.player.hitbox } returns Rectangle(0f, 0f, 0.5f, 2f)
        assertFalse(action.perform(open, 0, 0))
        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `closing cannot create a collider inside a mob`() {
        every { mobs.mobs } returns listOf(mockk { every { hitbox } returns Rectangle(0f, 0f, 0.5f, 1f) })
        assertFalse(action.perform(open, 0, 0))
        verify(exactly = 0) { world.setForeMap(any(), any(), any(), any(), any()) }
    }

    private fun gate(open: Boolean): Block.Normal {
        val params = mockk<CommonBlockParams>(relaxed = true)
        every { params.key } returns "fence_gate_oak_left_${if (open) "open" else "closed"}"
        every { params.hasCollision } returns !open
        every { params.useActionKey } returns FenceBlocks.USE_GATE_ACTION_KEY
        every { params.collisionMargins } returns BlockInsets.Meters(0f, 0f, if (open) 0f else 0.75f, 0f)
        return Block.Normal(params)
    }
}
