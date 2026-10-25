package ru.fredboy.cavedroid.entity.vehicle

import com.badlogic.gdx.math.Rectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BoatPhysicsTest {
    @Test
    fun `hits start a small upward bob without accumulating launch speed`() {
        var velocity = BoatPhysics.hitVerticalVelocity(0f)
        assertEquals(-BoatTuning.HIT_BOB_SPEED, velocity)
        repeat(20) { velocity = BoatPhysics.hitVerticalVelocity(velocity) }
        assertEquals(-BoatTuning.HIT_BOB_SPEED, velocity)
    }

    @Test
    fun `hit reaction preserves faster rising motion and only nudges falling motion`() {
        assertEquals(-5f, BoatPhysics.hitVerticalVelocity(-5f))
        assertEquals(5f - BoatTuning.HIT_BOB_SPEED, BoatPhysics.hitVerticalVelocity(5f))
    }

    @Test
    fun `one third draft balances gravity`() {
        assertEquals(-32f, BoatPhysics.verticalAcceleration(BoatTuning.DRAFT, 0f, 32f), 0.0001f)
    }

    @Test
    fun `partial water contributes only overlapping area`() {
        val hull = Rectangle(0f, 0.5f, 1.5f, 0.5f)
        assertEquals(0.25f, BoatPhysics.overlapArea(hull, Rectangle(0f, 0.75f, 1f, 0.25f)))
        assertEquals(0f, BoatPhysics.overlapArea(hull, Rectangle(0f, 1f, 1f, 1f)))
    }

    @Test
    fun `dry hull has no buoyancy or rowing`() {
        assertEquals(0f, BoatPhysics.verticalAcceleration(0f, 8f, 32f))
        assertEquals(2f, BoatPhysics.horizontalVelocity(2f, 1f, 0f, 1f))
    }

    @Test
    fun `rowing reaches but does not exceed speed limit`() {
        var velocity = 0f
        repeat(600) { velocity = BoatPhysics.horizontalVelocity(velocity, 1f, 0.2f, 1f / 60f) }
        assertEquals(6f, velocity)
        repeat(1200) { velocity = BoatPhysics.horizontalVelocity(velocity, -1f, 0.2f, 1f / 60f) }
        assertEquals(-6f, velocity)
    }

    @Test
    fun `released controls coast with frame independent drag`() {
        fun coast(steps: Int): Float {
            var speed = 6f
            repeat(steps) { speed = BoatPhysics.horizontalVelocity(speed, 0f, 0.3f, 1f / steps) }
            return speed
        }
        assertTrue(coast(60) in 0f..6f)
        assertEquals(coast(30), coast(144), 0.0001f)
    }

    @Test
    fun `vertical damping opposes sinking and rising`() {
        val static = BoatPhysics.verticalAcceleration(0.5f, 0f, 32f)
        assertTrue(BoatPhysics.verticalAcceleration(0.5f, 2f, 32f) < static)
        assertTrue(BoatPhysics.verticalAcceleration(0.5f, -2f, 32f) > static)
    }

    @Test
    fun `water removal restores free fall`() {
        assertEquals(0f, BoatPhysics.verticalAcceleration(0f, -4f, 32f))
    }
}
