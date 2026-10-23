package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class UseFenceGateItemActionTest {

    private val world = mockk<GameWorld>(relaxed = true)
    private val repository = mockk<ItemsRepository>()
    private val mobs = mockk<MobController>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val stats = mockk<StatsRepository>(relaxed = true)
    private val item = mockk<Item.Usable> { every { params.key } returns "fence_gate_birch" }
    private val left = mockk<Block.Normal> { every { getRectangle(5, 5) } returns Rectangle(5f, 5f, 0.25f, 1f) }
    private val right = mockk<Block.Normal> { every { getRectangle(5, 5) } returns Rectangle(5.75f, 5f, 0.25f, 1f) }
    private val action = UseFenceGateItemAction(world, mobs, GetBlockByKeyUseCase(repository), stats)

    init {
        every { mobs.player } returns player
        every { repository.getBlockByKey("fence_gate_birch_left_closed") } returns left
        every { repository.getBlockByKey("fence_gate_birch_right_closed") } returns right
        every { player.hitbox } returns Rectangle(0f, 0f, 1f, 2f)
        every { mobs.mobs } returns emptyList()
    }

    @Test
    fun `looking right places the left closed variant and consumes one item`() {
        every { player.direction } returns Direction.RIGHT
        every { world.placeToForeground(5, 5, left, false, false) } returns true
        assertTrue(action.perform(item, 5, 5))
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
        verify(exactly = 1) { stats.recordBlockPlaced() }
    }

    @Test
    fun `looking left places the right closed variant`() {
        every { player.direction } returns Direction.LEFT
        every { world.placeToForeground(5, 5, right, false, false) } returns true
        assertTrue(action.perform(item, 5, 5))
        verify { world.placeToForeground(5, 5, right, false, false) }
    }

    @Test
    fun `failed placement keeps the item and statistics`() {
        every { player.direction } returns Direction.RIGHT
        every { world.placeToForeground(5, 5, left, false, false) } returns false
        assertFalse(action.perform(item, 5, 5))
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
        verify(exactly = 0) { stats.recordBlockPlaced() }
    }
}
