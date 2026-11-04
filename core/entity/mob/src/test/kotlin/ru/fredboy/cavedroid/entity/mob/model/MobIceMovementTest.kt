package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import kotlin.math.abs

class MobIceMovementTest {
    private val motion = Vector2()
    private val body = mockk<Body>(relaxed = true) {
        every { position } returns Vector2(-2.5f, 10.085f)
        every { linearVelocity } answers { motion.cpy() }
        every { linearVelocity = any() } answers { motion.set(firstArg<Vector2>()) }
    }
    private val physics = mockk<MobPhysicsFactory> {
        every { createBody(any(), any(), any(), any(), any()) } returns body
    }
    private val ice = mockk<Block> { every { params.key } returns "ice" }
    private val stone = mockk<Block> { every { params.key } returns "stone" }
    private val world = mockk<MobWorldAdapter>(relaxed = true) {
        every { isInfinite } returns true
        every { height } returns 256
        every { getClimbable(any()) } returns null
        every { getForegroundBlock(any(), any()) } returns stone
        every { getForegroundBlock(-3, 11) } returns ice
    }
    private val mob = TestMob().apply {
        spawn(-2.5f, 10.085f, physics)
        footContactCounter = 1
    }

    @Test
    fun `mobs coast on ice in either direction and come to rest`() {
        for (speed in listOf(4f, -4f)) {
            mob.controlVector.x = speed
            update()
            mob.controlVector.setZero()
            update()
            assertEquals(speed * 0.8f, motion.x, 0.0001f)
            repeat(10) { update() }
            assertEquals(0f, motion.x)
        }
    }

    @Test
    fun `mobs stop sliding when they reach ordinary ground`() {
        motion.x = 4f
        update()
        assertTrue(motion.x > 0f)
        every { world.getForegroundBlock(-3, 11) } returns stone
        update()
        assertEquals(0f, motion.x)
    }

    @Test
    fun `mob walking animation settles smoothly while coasting`() {
        motion.x = 4f
        mob.anim = -30f
        mob.draw(mockk(), 0f, 0f, 1f / 60f)
        assertTrue(abs(mob.anim) > 0f && abs(mob.anim) < 30f)
        repeat(10) { mob.draw(mockk(), 0f, 0f, 1f / 60f) }
        assertEquals(0f, mob.anim)
        assertEquals(4f, motion.x)
    }

    private fun update() = mob.update(world, mockk(relaxed = true), mockk(relaxed = true), 0.1f)

    // Keep real movement and animation logic without AI decisions or native rendering.
    private class TestMob :
        Mob(
            Direction.RIGHT,
            mockk<MobParams>(relaxed = true) {
                every { height } returns 1.8f
                every { speed } returns 4f
                every { animationRange } returns 60f
            },
            mockk(relaxed = true),
        ) {
        override fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float) = updateAnimation(delta)

        override fun changeDir() = Unit
    }
}
