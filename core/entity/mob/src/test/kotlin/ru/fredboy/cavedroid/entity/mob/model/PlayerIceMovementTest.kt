package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.usecase.GetFallbackItemUseCase
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import kotlin.math.abs

class PlayerIceMovementTest {
    private val motion = Vector2()
    private val location = Vector2(-2.5f, 10.085f)
    private val body = mockk<Body>(relaxed = true) {
        every { position } returns location
        every { linearVelocity } answers { motion.cpy() }
        every { linearVelocity = any() } answers {
            motion.set(firstArg<Vector2>())
            Unit
        }
    }
    private val physics = mockk<MobPhysicsFactory> {
        every { createBody(any(), any(), any(), any(), any()) } returns body
    }
    private val ice = mockk<Block> {
        every { params.key } returns "ice"
    }
    private val stone = mockk<Block> {
        every { params.key } returns "stone"
    }
    private val world = mockk<MobWorldAdapter>(relaxed = true) {
        every { isInfinite } returns true
        every { height } returns 256
        every { getClimbable(any()) } returns null
        every { getForegroundBlock(any(), any()) } returns stone
        every { getForegroundBlock(-3, 11) } returns ice
    }
    private val player = spyk(
        Player(
            getFallbackItem = mockk<GetFallbackItemUseCase> {
                every { this@mockk.invoke() } returns Item.None(mockk(relaxed = true))
            },
            tooltipManager = mockk(relaxed = true),
            params = mockk<MobParams>(relaxed = true) {
                every { hp } returns 20
                every { height } returns 1.8f
                every { speed } returns 4f
                every { animationRange } returns 60f
                every { sprites } returns emptyList()
            },
            soundPlayer = mockk(relaxed = true),
            stepsSoundAssetsRepository = mockk(relaxed = true),
        ).apply { spawn(location.x, location.y, physics) },
    ).apply {
        gameMode = GameMode.CREATIVE
        footContactCounter = 1
        // Cursor ray casting needs a native world; movement uses the mocked body.
        every { rayCastCursor(any(), any()) } just Runs
    }

    @Test
    fun `releasing movement on ice preserves momentum in either direction`() {
        for (speed in listOf(4f, -4f)) {
            player.controlVector.x = speed
            update(0.1f)
            player.controlVector.setZero()
            update(0.1f)
            assertEquals(speed * 0.8f, motion.x, 0.0001f)
        }
    }

    @Test
    fun `sliding gradually stops without reversing direction`() {
        motion.set(-1f, 0f)
        update(0.1f)
        assertTrue(motion.x > -1f && motion.x < 0f)
        repeat(10) { update(0.1f) }
        assertEquals(0f, motion.x)
    }

    @Test
    fun `deceleration is independent of update frequency`() {
        motion.x = 4f
        update(0.5f)
        val singleStep = motion.x
        motion.x = 4f
        repeat(30) { update(1f / 60f) }
        assertEquals(singleStep, motion.x, 0.0001f)
    }

    @Test
    fun `leaving ice for ordinary ground stops sliding`() {
        motion.x = 4f
        update(0.1f)
        every { world.getForegroundBlock(-3, 11) } returns stone
        update(0.1f)
        assertEquals(0f, motion.x)
    }

    @Test
    fun `ice below an airborne player does not cause sliding`() {
        player.footContactCounter = 0
        motion.x = 4f
        update(0.1f)
        assertEquals(0f, motion.x)
    }

    @Test
    fun `movement input still controls direction on ice`() {
        motion.x = 4f
        player.controlVector.x = -4f
        update(0.1f)
        assertEquals(-4f, motion.x)
    }

    @Test
    fun `releasing movement smoothly settles either animation phase while sliding`() {
        val batch = mockk<SpriteBatch>()
        for (phase in listOf(30f, -30f)) {
            for (animationDirection in listOf(360f, -360f)) {
                player.anim = phase
                player.animDelta = animationDirection
                motion.x = 4f
                player.controlVector.setZero()
                update(1f / 60f)
                player.draw(batch, 0f, 0f, 1f / 60f)
                assertTrue(abs(player.anim) > 0f && abs(player.anim) < abs(phase))
                repeat(10) { player.draw(batch, 0f, 0f, 1f / 60f) }
                assertEquals(0f, player.anim)
                assertTrue(motion.x > 0f)
            }
        }
    }

    @Test
    fun `walking animation resumes when movement input resumes`() {
        player.anim = 0f
        motion.x = 4f
        player.controlVector.x = 4f
        player.draw(mockk<SpriteBatch>(), 0f, 0f, 1f / 60f)
        assertTrue(abs(player.anim) > 0f)
    }

    private fun update(delta: Float) = player.update(world, mockk(relaxed = true), mockk(relaxed = true), delta)
}
