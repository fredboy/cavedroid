package ru.fredboy.cavedroid.entity.mob.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreeperFuseTest {
    private val fuse = CreeperFuse()

    @Test
    fun `nearby visible survival player gets a full one and a half second warning`() {
        fuse.update(1f, true, false, 1f)
        assertTrue(fuse.started)
        assertFalse(fuse.isReady)
        fuse.update(1f, true, false, 0.5f)
        assertTrue(fuse.isReady)
        assertFalse(fuse.started)
    }

    @Test
    fun `distant hidden and creative players never start an automatic fuse`() {
        fuse.update(3f, true, false, 2f)
        assertFalse(fuse.isPrimed)
        fuse.update(1f, false, false, 2f)
        assertFalse(fuse.isPrimed)
        fuse.update(1f, true, true, 2f)
        assertFalse(fuse.isPrimed)
    }

    @Test
    fun `creeper must close to less than one and a half blocks before priming`() {
        fuse.update(2f, true, false, 2f)
        assertFalse(fuse.isPrimed)
        fuse.update(1.5f, true, false, 2f)
        assertFalse(fuse.isPrimed)
        fuse.update(1.49f, true, false, 0.1f)
        assertTrue(fuse.started)
        assertEquals(0.1f, fuse.elapsedSec)
    }

    @Test
    fun `fuse continues out to seven blocks but cancels on escape`() {
        fuse.update(1f, true, false, 0.5f)
        fuse.update(6f, true, false, 0.5f)
        assertEquals(1f, fuse.elapsedSec)
        fuse.update(7f, true, false, 1f)
        assertFalse(fuse.isPrimed)
    }

    @Test
    fun `losing sight cancels immediately and a new encounter starts a fresh warning`() {
        fuse.update(1f, true, false, 1.4f)
        fuse.update(1f, false, false, 0.2f)
        assertFalse(fuse.isPrimed)
        fuse.update(1f, true, false, 0.1f)
        assertEquals(0.1f, fuse.elapsedSec)
        assertTrue(fuse.started)
    }

    @Test
    fun `switching to creative cancels automatic detonation`() {
        fuse.update(1f, true, false, 1.4f)
        fuse.update(1f, true, true, 0.2f)
        assertFalse(fuse.isPrimed)
    }

    @Test
    fun `flint and steel ignition continues even after escape or switching to creative`() {
        fuse.forced = true
        fuse.update(30f, false, true, 1.5f)
        assertTrue(fuse.isReady)
    }

    @Test
    fun `saved partial fuse retains remaining warning time`() {
        fuse.elapsedSec = 1f
        fuse.update(4f, true, false, 0.25f)
        assertFalse(fuse.started)
        assertFalse(fuse.isReady)
        fuse.update(4f, true, false, 0.25f)
        assertTrue(fuse.isReady)
    }
}
