package ru.fredboy.cavedroid.gameplay.controls.action.placeblock

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.vehicle.VehicleController
import ru.fredboy.cavedroid.game.world.GameWorld

class PlaceBlockItemToForegroundActionTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val vehicles = mockk<VehicleController>(relaxed = true)
    private val stats = mockk<StatsRepository>(relaxed = true)
    private val block = mockk<Block.Normal>(relaxed = true)
    private val item = Item.Block(mockk(), block)
    private val bounds = Rectangle(33f, 20f, 1f, 1f)
    private val action = PlaceBlockItemToForegroundAction(world, mockk(), mobs, mockk(relaxed = true), mockk(), stats, mockk(), vehicles)

    init {
        every { mobs.player } returns player
        every { block.params.hasCollision } returns true
        every { block.params.material } returns null
        every { block.getRectangle(33, 20) } returns bounds
        every { player.hitbox } returns Rectangle(30f, 19f, 0.6f, 1.8f)
        every { mobs.mobs } returns emptyList()
        every { world.placeToForeground(33, 20, block, false, false) } returns true
    }

    @Test
    fun `solid block overlapping a boat is rejected without consuming the item`() {
        every { vehicles.overlapsVehicle(bounds) } returns true
        assertFalse(action.place(item, 33, 20))
        verify(exactly = 0) { world.placeToForeground(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
        verify(exactly = 0) { stats.recordBlockPlaced() }
    }

    @Test
    fun `solid block clear of boats can be placed`() {
        assertTrue(action.place(item, 33, 20))
        verify { player.decreaseCurrentItemCount(1) }
        verify { stats.recordBlockPlaced() }
    }

    @Test
    fun `passable block can overlap a boat`() {
        every { block.params.hasCollision } returns false
        every { vehicles.overlapsVehicle(bounds) } returns true
        assertTrue(action.place(item, 33, 20))
    }
}
