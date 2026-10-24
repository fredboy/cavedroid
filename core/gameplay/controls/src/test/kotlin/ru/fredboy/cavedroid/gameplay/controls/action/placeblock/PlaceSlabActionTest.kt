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

class PlaceSlabActionTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController>(relaxed = true)
    private val vehicles = mockk<VehicleController>(relaxed = true)
    private val stats = mockk<StatsRepository>(relaxed = true)
    private val top = mockk<Block.Slab>()
    private val bottom = mockk<Block.Slab>()
    private val item = Item.Slab(mockk(), top, bottom)
    private val topBounds = Rectangle(33f, 20f, 1f, 0.5f)
    private val bottomBounds = Rectangle(33f, 20.5f, 1f, 0.5f)
    private val action = PlaceSlabAction(world, mobs, stats, vehicles)

    init {
        every { mobs.player } returns player
        every { top.getRectangle(33, 20) } returns topBounds
        every { bottom.getRectangle(33, 20) } returns bottomBounds
        every { player.hitbox } returns Rectangle(30f, 19f, 0.6f, 1.8f)
        every { mobs.mobs } returns emptyList()
        every { world.placeToForeground(33, 20, any(), false, false) } returns true
    }

    @Test
    fun `bottom slab overlapping a boat is rejected`() {
        every { vehicles.overlapsVehicle(bottomBounds) } returns true
        assertFalse(action.place(item, 33, 20))
        verifyNoPlacement()
    }

    @Test
    fun `adding a top slab over a bottom slab cannot embed a boat`() {
        every { world.getForeMap(33, 20) } returns bottom
        every { vehicles.overlapsVehicle(topBounds) } returns true
        assertFalse(action.place(item, 33, 20))
        verifyNoPlacement()
    }

    @Test
    fun `slab clear of boats can be placed`() {
        assertTrue(action.place(item, 33, 20))
        verify { world.placeToForeground(33, 20, bottom, false, false) }
        verify { player.decreaseCurrentItemCount(1) }
    }

    private fun verifyNoPlacement() {
        verify(exactly = 0) { world.placeToForeground(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
        verify(exactly = 0) { stats.recordBlockPlaced() }
    }
}
