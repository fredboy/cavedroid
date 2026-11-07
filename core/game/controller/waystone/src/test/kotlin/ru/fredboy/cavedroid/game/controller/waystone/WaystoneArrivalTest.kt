package ru.fredboy.cavedroid.game.controller.waystone

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WaystoneArrivalTest {
    @Test
    fun `arrival prefers standing above the pad and leaves a small collision gap`() {
        val result = WaystoneArrival.find(10, 64, .7f, 1.8f, { it }, { true }, { it.y < 64 && it.y + it.height > 64 })!!
        assertEquals(10.5f, result.x)
        assertEquals(63.08f, result.y, .001f)
    }

    @Test
    fun `blocked top searches nearby clear ground and canonicalizes looping coordinates`() {
        val result = WaystoneArrival.find(0, 64, .7f, 1.8f, { Math.floorMod(it, 10) }, { it.x > 9 }, { true })!!
        assertEquals(9.5f, result.x)
    }

    @Test
    fun `clear air without supporting ground is not a safe arrival`() {
        assertNull(WaystoneArrival.find(-100000, 64, .7f, 1.8f, { it }, { true }, { false }))
        assertNull(WaystoneArrival.find(-100000, 64, .7f, 1.8f, { it }, { false }, { true }))
    }
}
