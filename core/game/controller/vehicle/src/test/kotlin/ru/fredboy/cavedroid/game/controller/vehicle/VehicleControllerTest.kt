package ru.fredboy.cavedroid.game.controller.vehicle

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Filter
import com.badlogic.gdx.physics.box2d.RayCastCallback
import com.badlogic.gdx.utils.Array
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.entity.vehicle.BoatTuning
import ru.fredboy.cavedroid.entity.vehicle.BoatType
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
    private val boat = Vehicle(state.id, state.type, mockk(relaxed = true))

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

    @ParameterizedTest
    @EnumSource(BoatType::class)
    fun `restoration preserves each supported wood type`(type: BoatType) {
        val saved = state.copy(type = type.itemKey)
        every { factory.create(saved) } returns Vehicle(saved.id, saved.type, mockk())
        controller.restore(listOf(saved))
        assertEquals(type.itemKey, controller.vehicles.single().type)
    }

    @ParameterizedTest
    @EnumSource(BoatType::class)
    fun `placement creates the requested wood type`(type: BoatType) {
        prepareTarget(playerX = 5f, boatX = 0f, cursorX = 8f)
        val ground = mockk<Block.Normal>(relaxed = true)
        every { ground.params.hasCollision } returns true
        every { ground.getRectangle(any(), any()) } answers { Rectangle(firstArg<Int>().toFloat(), secondArg<Int>().toFloat(), 1f, 1f) }
        every { world.getForeMap(any(), 11) } returns ground
        every { player.hitbox } returns Rectangle(4.7f, 9f, 0.6f, 1.8f)
        every { factory.create(match { it.id != state.id }) } answers {
            val placed = firstArg<VehicleState>()
            Vehicle(placed.id, placed.type, mockk())
        }

        assertTrue(controller.place(8f, 10.75f, type.itemKey))
        assertEquals(type.itemKey, controller.vehicles.last().type)
        verify(exactly = 1) { player.decreaseCurrentItemCount(1) }
    }

    @Test
    fun `unsupported boat placement does not consume an item`() {
        assertFalse(controller.place(5f, 10f, "unknown"))
        verify(exactly = 0) { factory.create(any()) }
        verify(exactly = 0) { player.decreaseCurrentItemCount(any()) }
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

    @Test
    fun `boarding targets the boat image across the left seam`() {
        prepareTarget(playerX = 1f, boatX = 1022f, cursorX = -2f)
        assertTrue(controller.boardTarget())
        verify { player.mountedVehicleId = boat.id }
        verify { world.world.rayCast(any(), any<Vector2>(), eq(Vector2(-2f, 10f))) }
    }

    @Test
    fun `attacking targets the boat image across the right seam`() {
        prepareTarget(playerX = 1023f, boatX = 2f, cursorX = 1026f)
        assertTrue(controller.attackTarget())
        assertEquals(BoatTuning.MAX_HEALTH - 1, boat.health)
        verify { world.world.rayCast(any(), any<Vector2>(), eq(Vector2(1026f, 10f))) }
    }

    @Test
    fun `wrapped targeting still rejects a blocking terrain fixture`() {
        prepareTarget(playerX = 1f, boatX = 1022f, cursorX = -2f)
        every { world.world.rayCast(any(), any<Vector2>(), any<Vector2>()) } answers {
            firstArg<RayCastCallback>().reportRayFixture(
                mockk {
                    every { isSensor } returns false
                    every { filterData } returns Filter().apply { categoryBits = PhysicsConstants.CATEGORY_BLOCK }
                },
                Vector2(),
                Vector2(),
                0.5f,
            )
            Unit
        }
        assertFalse(controller.boardTarget())
        assertFalse(controller.attackTarget())
    }

    @Test
    fun `infinite worlds do not wrap distant boats into reach`() {
        prepareTarget(playerX = 1f, boatX = 1022f, cursorX = 1022f)
        every { world.isInfinite } returns true
        assertFalse(controller.boardTarget())
        assertFalse(controller.attackTarget())
    }

    @Test
    fun `sleeping player cannot board a boat`() {
        prepareTarget(playerX = 1f, boatX = 3f, cursorX = 3f)
        every { player.isInBed } returns true
        assertFalse(controller.boardTarget())
    }

    @Test
    fun `vehicle overlap includes mirrored hulls only in finite worlds`() {
        prepareTarget(playerX = 1f, boatX = 1022f, cursorX = -2f)
        val bounds = Rectangle(-2f, 10f, 1f, 1f)
        assertTrue(controller.overlapsVehicle(bounds))
        every { world.isInfinite } returns true
        assertFalse(controller.overlapsVehicle(bounds))
    }

    private fun prepareTarget(playerX: Float, boatX: Float, cursorX: Float) {
        every { world.width } returns 1024
        every { world.height } returns 128
        every { world.isChunkLoadedAt(any()) } returns true
        every { world.getForeMap(any(), any()).params.hasCollision } returns false
        every { mobs.mobs } returns emptyList()
        every { player.position } returns Vector2(playerX, 10f)
        every { player.cursorX } returns cursorX
        every { player.cursorY } returns 10f
        every { player.width } returns 0.6f
        every { player.height } returns 1.8f
        every { player.gameMode } returns GameMode.SURVIVAL
        every { player.mountedVehicleId } returns null
        every { player.controlVector } returns Vector2()
        every { player.body.fixtureList } returns Array()
        every { boat.body.position } returns Vector2(boatX, 10f)
        every { boat.body.linearVelocity } returns Vector2()
        every { factory.setOccupied(boat, any(), any(), any()) } answers { boat.occupied = arg(3) }
        controller.restore(listOf(state))
    }
}
