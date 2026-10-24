package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.VillagerMob

class VillagerMobBehaviorTest {
    private val position = Vector2(-14f, 20.0625f)
    private val body = mockk<Body>(relaxed = true) {
        every { position } answers { this@VillagerMobBehaviorTest.position }
        every { linearVelocity } returns Vector2()
    }
    private val physics = mockk<MobPhysicsFactory> {
        every { createBody(any(), any(), any(), any(), any()) } returns body
    }
    private val world = mockk<MobWorldAdapter>(relaxed = true) {
        every { isInfinite } returns true
        every { isDayTime() } returns true
        every { isChunkLoadedAt(any()) } returns true
    }
    private val params = MobParams("Villager", "villager", 0.5f, 1.875f, 1.5f, MobBehaviorType.VILLAGER, emptyList(), 20, emptyList(), 20f, 0, false, 20, 1f, 0f)
    private val mob = VillagerMob(params).apply {
        home = VillagerMob.BlockPosition(-15, 20)
        spawn(-14f, 20.0625f, physics)
        footContactCounter = 1
    }

    init {
        every { world.getForegroundBlock(any(), any()) } answers {
            when {
                secondArg<Int>() >= 21 -> block("cobblestone", true)
                firstArg<Int>() == -15 && secondArg<Int>() == 20 -> block("bed_l")
                firstArg<Int>() == -14 && secondArg<Int>() == 20 -> block("bed_r")
                else -> block("none")
            }
        }
    }

    @Test
    fun `idle residents eventually choose a daytime walk`() {
        var moved = false
        repeat(200) {
            update()
            moved = moved || mob.controlVector.x != 0f
        }
        assertTrue(moved)
    }

    @Test
    fun `an unloaded home is not treated as a broken bed`() {
        every { world.isChunkLoadedAt(-15) } returns false
        every { world.getForegroundBlock(-15, 20) } returns block("none")
        update()
        assertFalse(mob.isDead)
        assertTrue(mob.controlVector.isZero)
    }

    @Test
    fun `destroying either half of a loaded bed despawns its resident`() {
        every { world.getForegroundBlock(-14, 20) } returns block("none")
        update()
        assertTrue(mob.isDead)
    }

    @Test
    fun `night sends residents home and sunrise wakes them`() {
        every { world.isDayTime() } returns false
        position.x = -9f
        update()
        assertTrue(mob.controlVector.x < 0f)
        assertFalse(mob.isSleeping)
        position.x = -14f
        update()
        assertTrue(mob.isSleeping)
        assertTrue(mob.controlVector.isZero)
        every { world.isDayTime() } returns true
        update()
        assertFalse(mob.isSleeping)
    }

    @Test
    fun `daytime wandering turns back at the home radius`() {
        position.x = -3f
        update()
        assertEquals(-params.speed, mob.controlVector.x)
        position.x = -25f
        update()
        assertEquals(params.speed, mob.controlVector.x)
    }

    @Test
    fun `a blocked doorway remains pending until closing succeeds`() {
        val door = VillagerMob.BlockPosition(-19, 20)
        mob.openedDoors.add(door)
        val openDoor = block("door_wood_bottom_left_open")
        every { world.getForegroundBlock(-19, 20) } returns openDoor
        every { world.setWoodenDoorOpen(-19, 20, false) } returns false
        update()
        assertEquals(setOf(door), mob.openedDoors)
        verify(exactly = 0) { world.playBlockActionSound(any(), any(), any(), any()) }
        every { world.setWoodenDoorOpen(-19, 20, false) } returns true
        update()
        assertTrue(mob.openedDoors.isEmpty())
        update()
        verify(exactly = 2) { world.setWoodenDoorOpen(-19, 20, false) }
        verify(exactly = 1) { world.playBlockActionSound(openDoor, -19, 20, any()) }
    }

    @Test
    fun `collision skin does not turn a one block downhill step into a cliff`() {
        position.set(-9f, 20.05f)
        every { world.isDayTime() } returns false
        every { world.getForegroundBlock(-10, any()) } answers { block("cobblestone", secondArg<Int>() >= 22) }
        update()
        assertTrue(mob.controlVector.x < 0f)
    }

    @Test
    fun `residents stop before drops deeper than a village step`() {
        position.set(-9f, 20.05f)
        every { world.isDayTime() } returns false
        every { world.getForegroundBlock(-10, any()) } answers { block("cobblestone", secondArg<Int>() >= 23) }
        update()
        assertTrue(mob.controlVector.isZero)
    }

    @Test
    fun `opening a paired door plays its action sound only once`() {
        position.x = -9f
        every { world.isDayTime() } returns false
        val closedDoor = block("door_wood_bottom_left_closed")
        val openDoor = block("door_wood_bottom_left_open")
        var opened = false
        every { world.getForegroundBlock(-10, 20) } answers { if (opened) openDoor else closedDoor }
        every { world.setWoodenDoorOpen(-10, 20, true) } answers {
            opened = true
            true
        }
        repeat(3) { update() }
        verify(exactly = 1) { world.playBlockActionSound(closedDoor, -10, 20, any()) }
        verify(exactly = 1) { world.setWoodenDoorOpen(-10, 20, true) }
    }

    private fun update() = mob.behavior.update(mob, world, mockk(), mockk(), 0.1f)

    private fun block(key: String, solid: Boolean = false): Block = mockk(relaxed = true) {
        every { params.key } returns key
        every { params.hasCollision } returns solid
    }
}
