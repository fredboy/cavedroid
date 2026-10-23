package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StarFieldTest {

    @Test
    fun `stars fade between midnight and twilight and disappear during the day`() {
        assertEquals(1f, StarField.opacity(0f))
        assertEquals(0.5f, StarField.opacity(0.25f))
        assertEquals(0f, StarField.opacity(0.5f))
        assertEquals(0f, StarField.opacity(1f))
    }

    @Test
    fun `both halves of the moon occlude stars regardless of phase`() {
        assertTrue(StarField.overlapsMoon(7f, 10f, 1f, 10f, 10f, 4f))
        assertTrue(StarField.overlapsMoon(12f, 10f, 1f, 10f, 10f, 4f))
    }

    @Test
    fun `stars touching the disc are hidden even when their centers are outside`() {
        assertTrue(StarField.overlapsMoon(5f, 9f, 1f, 10f, 10f, 4f))
        assertTrue(StarField.overlapsMoon(6f, 6f, 2f, 10f, 10f, 4f))
        assertFalse(StarField.overlapsMoon(5f, 5f, 1f, 10f, 10f, 4f))
        assertFalse(StarField.overlapsMoon(14.1f, 10f, 1f, 10f, 10f, 4f))
    }

    @Test
    fun `moon occlusion uses the same geometry after moving across the sky`() {
        assertTrue(StarField.overlapsMoon(107f, -40f, 1f, 110f, -40f, 4f))
        assertFalse(StarField.overlapsMoon(7f, 10f, 1f, 110f, -40f, 4f))
    }

    @Test
    fun `star positions remain stable between sessions`() {
        assertEquals(StarField(0x57A2).stars, StarField(0x57A2).stars)
    }
}
