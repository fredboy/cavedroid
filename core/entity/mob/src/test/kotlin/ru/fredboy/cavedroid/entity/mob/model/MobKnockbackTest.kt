package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter

class MobKnockbackTest {
    private val motion = Vector2()
    private val body = mockk<Body>(relaxed = true) {
        every { position } returns Vector2(10f, 10f)
        every { linearVelocity } answers { motion.cpy() }
        every { linearVelocity = any() } answers {
            motion.set(firstArg<Vector2>())
            Unit
        }
    }
    private val physics = mockk<MobPhysicsFactory> {
        every { createBody(any(), any(), any(), any(), any()) } returns body
    }
    private val world = mockk<MobWorldAdapter>(relaxed = true) {
        every { isInfinite } returns true
        every { height } returns 256
        every { getClimbable(any()) } returns null
    }
    private val mob = TestMob().apply { spawn(10f, 10f, physics) }

    @Test
    fun `combat hits push away in either direction and lift upwards`() {
        mob.takeHit(1, 3f)
        assertEquals(19, mob.health)
        assertTrue(motion.x > 0f)
        assertTrue(motion.y < 0f)
        val rightSpeed = motion.x

        mob.takeHit(1, -3f)
        assertEquals(-rightSpeed, motion.x)
        assertTrue(motion.y < 0f)
    }

    @Test
    fun `walking input cannot overwrite recoil and resumes after recovery`() {
        mob.controlVector.x = -2f
        mob.takeHit(1, 1f)
        val recoil = motion.cpy()
        update(0.1f)
        assertEquals(recoil, motion)
        update(0.11f)
        update(0.01f)
        assertEquals(-2f, motion.x)
    }

    @Test
    fun `stationary mobs preserve recoil and cannot jump during recovery`() {
        mob.footContactCounter = 1
        mob.takeHit(1, -1f)
        update(0.1f)
        assertTrue(motion.x < 0f)
        assertFalse(mob.canJump)
        update(0.11f)
        update(0.01f)
        assertEquals(0f, motion.x)
        assertTrue(mob.canJump)
    }

    @Test
    fun `ignored zero damage and dead targets do not receive knockback`() {
        mob.immune = true
        mob.takeHit(1, 1f)
        assertEquals(Vector2.Zero, motion)
        mob.immune = false
        mob.takeHit(0, 1f)
        assertEquals(Vector2.Zero, motion)
        mob.isDead = true
        mob.takeHit(1, 1f)
        assertEquals(Vector2.Zero, motion)
        assertEquals(20, mob.health)
    }

    @Test
    fun `environmental damage does not add a directional impulse`() {
        mob.damage(1)
        assertEquals(19, mob.health)
        assertEquals(Vector2.Zero, motion)
    }

    @Test
    fun `repeated hits do not stack launch speed or erase an existing upward jump`() {
        motion.set(0f, -8f)
        mob.takeHit(1, 1f)
        val first = motion.cpy()
        repeat(3) { mob.takeHit(1, 1f) }
        assertEquals(first, motion)
        assertEquals(-8f, motion.y)
    }

    @Test
    fun `vertical hits lift without inventing a horizontal direction`() {
        mob.takeHit(1, 0f)
        assertEquals(0f, motion.x)
        assertTrue(motion.y < 0f)
    }

    private fun update(delta: Float) = mob.update(world, mockk(relaxed = true), mockk(relaxed = true), delta)

    // Damage timing and rendering need libGDX; this fake keeps the real hit and movement integration.
    private class TestMob :
        Mob(
            Direction.RIGHT,
            mockk<MobParams>(relaxed = true) { every { hp } returns 20 },
            mockk(relaxed = true),
        ) {
        var immune = false

        override fun damage(damage: Int) {
            if (!immune) health -= damage
        }

        override fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float) = Unit

        override fun changeDir() = Unit
    }
}
