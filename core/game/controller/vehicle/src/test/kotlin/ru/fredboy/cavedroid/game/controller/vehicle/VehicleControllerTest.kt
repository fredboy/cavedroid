package ru.fredboy.cavedroid.game.controller.vehicle

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.entity.vehicle.Vehicle
import ru.fredboy.cavedroid.entity.vehicle.VehicleState
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

class VehicleControllerTest {
    private val world = mockk<GameWorld>(relaxed = true)
    private val player = mockk<Player>(relaxed = true)
    private val mobs = mockk<MobController> { every { player } returns this@VehicleControllerTest.player }
    private val factory = mockk<BoatPhysicsFactory>()
    private val controller = VehicleController(world, mobs, factory, mockk(), mockk(), mockk(), mockk())
    private val state = VehicleState("stable-id", x = 5f, y = 10f)
    private val boat = Vehicle(state.id, state.type, mockk())

    init {
        every { factory.create(any()) } returns boat
    }

    @Test
    fun `duplicate ids in a snapshot and repeated restoration create one body`() {
        controller.restore(listOf(state, state))
        controller.restore(listOf(state))
        assertEquals(listOf(boat), controller.vehicles.toList())
        verify(exactly = 1) { factory.create(state) }
    }

    @Test
    fun `unsupported or invalid records never create native bodies`() {
        controller.restore(listOf(state.copy(type = "unknown"), state.copy(x = Float.NaN), state.copy(health = 0)))
        assertEquals(0, controller.vehicles.size)
        verify(exactly = 0) { factory.create(any()) }
    }

    @Test
    fun `occupied boat cannot be evicted`() {
        controller.restore(listOf(state))
        boat.occupied = true
        controller.evict(boat)
        assertEquals(1, controller.vehicles.size)
    }

    @Test
    fun `missing mounted reference restores an unmounted player`() {
        every { player.mountedVehicleId } returns "missing"
        controller.resolvePassenger()
        verify { player.mountedVehicleId = null }
    }
}
