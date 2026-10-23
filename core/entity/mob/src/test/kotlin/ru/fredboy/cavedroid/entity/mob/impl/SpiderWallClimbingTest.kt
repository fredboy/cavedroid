package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.block.Block

class SpiderWallClimbingTest {

    private val hitbox = Rectangle(0.02f, 0f, 1.96f, 1f)
    private val air = mockk<Block> {
        every { params.hasCollision } returns false
    }

    private fun blockAt(x: Int, y: Int, blockHeight: Float = 1f): (Int, Int) -> Block {
        val wall = mockk<Block> {
            every { params.hasCollision } returns true
            every { getRectangle(x, y) } returns Rectangle(x.toFloat(), y + 1f - blockHeight, 1f, blockHeight)
        }
        return { bx, by -> if (bx == x && by == y) wall else air }
    }

    @Test
    fun `spider can climb a wall on either side including negative coordinates`() {
        assertTrue(canSpiderClimbWall(hitbox, 1f, blockAt(2, 0)))
        assertTrue(canSpiderClimbWall(hitbox, -1f, blockAt(-1, 0)))
    }

    @Test
    fun `spider does not climb while stationary or moving away from a wall`() {
        assertFalse(canSpiderClimbWall(hitbox, 0f, blockAt(2, 0)))
        assertFalse(canSpiderClimbWall(hitbox, -1f, blockAt(2, 0)))
    }

    @Test
    fun `floor and low step do not count as a wall`() {
        assertFalse(canSpiderClimbWall(hitbox, 1f, blockAt(2, 1)))
        assertFalse(canSpiderClimbWall(hitbox, 1f, blockAt(2, 0, blockHeight = 0.25f)))
    }

    @Test
    fun `spider stops climbing after losing wall contact`() {
        assertFalse(canSpiderClimbWall(hitbox, 1f) { _, _ -> air })
        assertFalse(canSpiderClimbWall(hitbox, 1f, blockAt(3, 0)))
    }

    @Test
    fun `wall detection works while airborne partway up a block`() {
        assertTrue(canSpiderClimbWall(Rectangle(0.02f, -0.5f, 1.96f, 1f), 1f, blockAt(2, 0)))
    }
}
