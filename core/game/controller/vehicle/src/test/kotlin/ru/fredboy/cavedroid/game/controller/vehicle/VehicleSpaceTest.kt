package ru.fredboy.cavedroid.game.controller.vehicle

import com.badlogic.gdx.math.Rectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VehicleSpaceTest {
    @Test
    fun `clear space above a supported boat remains an exit when both sides are blocked`() {
        val result = VehicleSpace.findExit(
            0f, 10f, 1, 0.6f, 1.8f,
            clear = { it.x == -0.3f }, supported = { false }, water = { false }, allowAbove = true,
        )
        assertEquals(0f, requireNotNull(result).x)
    }

    @Test
    fun `placement accepts land and partial water but rejects air and obstruction`() {
        assertTrue(VehicleSpace.canPlace(true, true, 0f))
        assertTrue(VehicleSpace.canPlace(true, false, 0.01f))
        org.junit.jupiter.api.Assertions.assertFalse(VehicleSpace.canPlace(true, false, 0f))
        org.junit.jupiter.api.Assertions.assertFalse(VehicleSpace.canPlace(false, true, 1f))
    }

    @Test
    fun `entity overlap is checked across finite seam`() {
        val hull = Rectangle(-0.5f, 0f, 1.5f, 0.5f)
        val other = Rectangle(99.5f, 0f, 1.5f, 0.5f)
        assertTrue(VehicleSpace.overlaps(hull, other, 100f))
        org.junit.jupiter.api.Assertions.assertFalse(VehicleSpace.overlaps(hull, other, null))
    }

    private fun exit(clear: (Rectangle) -> Boolean = { true }, supported: (Rectangle) -> Boolean = { true }, water: (Rectangle) -> Boolean = { false }) = VehicleSpace.findExit(0f, 10f, 1, 0.6f, 1.8f, clear, supported, water)

    @Test
    fun `exit prefers facing side`() {
        assertTrue(requireNotNull(exit()).x > 0)
    }

    @Test
    fun `blocked facing side exits on opposite side`() {
        assertTrue(requireNotNull(exit(clear = { it.x < 0 })).x < 0)
    }

    @Test
    fun `supported ground is preferred to water`() {
        assertTrue(requireNotNull(exit(supported = { it.x < -0.5f }, water = { true })).x < 0)
    }

    @Test
    fun `water exit is allowed without ground`() {
        assertTrue(requireNotNull(exit(supported = { false }, water = { true })).x > 0)
    }

    @Test
    fun `blocked exits keep passenger mounted`() {
        assertNull(exit(clear = { false }))
    }

    @Test
    fun `above is used when both sides are blocked`() {
        assertEquals(0f, requireNotNull(exit(clear = { it.x == -0.3f }, water = { true })).x)
    }

    @Test
    fun `empty air is not a safe exit`() {
        assertNull(exit(supported = { false }))
    }
}
