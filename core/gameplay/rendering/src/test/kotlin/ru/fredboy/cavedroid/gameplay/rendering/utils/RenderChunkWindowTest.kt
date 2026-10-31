package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.math.Rectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RenderChunkWindowTest {
    @Test
    fun `fractional movement across either seam does not change pool capacity`() {
        val viewport = Rectangle(0f, 0f, 31.25f, 14.05f)
        val capacity = RenderChunkWindow(viewport).capacity
        for (step in -128..128) {
            viewport.setPosition(step / 4f, step / 7f)
            val window = RenderChunkWindow(viewport)
            assertEquals(capacity, window.capacity)
            assertTrue((window.maxX - window.minX + 3) * (window.maxY - window.minY + 3) <= capacity)
        }
    }

    @Test
    fun `negative coordinates retain exactly one ring and drop tiles behind the view`() {
        val window = RenderChunkWindow(Rectangle(-0.5f, -0.5f, 16f, 16f))
        assertEquals(-1, window.minX)
        assertEquals(0, window.maxX)
        assertTrue(window.retains(-2, -2))
        assertTrue(window.retains(1, 1))
        assertFalse(window.retains(-3, 0))
        assertFalse(window.retains(0, 2))
    }
}
