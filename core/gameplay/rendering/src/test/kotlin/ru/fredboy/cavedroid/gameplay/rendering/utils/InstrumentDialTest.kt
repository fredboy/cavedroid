package ru.fredboy.cavedroid.gameplay.rendering.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InstrumentDialTest {
    @Test
    fun `clock follows sunrise noon sunset and midnight including after day rollover`() {
        for ((time, frame) in listOf(0f to 48, 300f to 0, 600f to 16, 900f to 32, 1200f to 48, 1500f to 0)) {
            assertEquals(frame, InstrumentDial.frame(InstrumentDial.clock(time, 1200f), 64))
        }
    }

    @Test
    fun `compass frames point in screen directions with Y increasing downward`() {
        for ((position, frame) in listOf((0f to 1f) to 0, (-1f to 0f) to 8, (0f to -1f) to 16, (1f to 0f) to 24)) {
            val turns = InstrumentDial.compass(0f, 0f, position.first, position.second)
            assertEquals(frame, InstrumentDial.frame(turns, 32))
        }
        assertEquals(20, InstrumentDial.frame(InstrumentDial.compass(0f, 0f, 1f, -1f), 32))
    }

    @Test
    fun `compass chooses shortest looping route even after multiple world widths`() {
        assertEquals(8, InstrumentDial.frame(InstrumentDial.compass(2f, 0f, 98f, 0f, 100f), 32))
        assertEquals(24, InstrumentDial.frame(InstrumentDial.compass(98f, 0f, 2f, 0f, 100f), 32))
        assertEquals(8, InstrumentDial.frame(InstrumentDial.compass(202f, 0f, -102f, 0f, 100f), 32))
        assertEquals(24, InstrumentDial.frame(InstrumentDial.compass(-100f, 0f, 0f, 0f), 32))
    }

    @Test
    fun `needle eases over frame zero and snaps when rendering a paused snapshot`() {
        val needle = InstrumentNeedle()
        assertEquals(0.99f, needle.update(0.99f, 0.016f))
        val next = needle.update(0.01f, 0.016f)
        assertTrue(next > 0.99f || next < 0.01f)
        assertEquals(0.5f, needle.update(0.5f, 0f))
        assertEquals(0, InstrumentDial.frame(0.999f, 32))
    }
}
