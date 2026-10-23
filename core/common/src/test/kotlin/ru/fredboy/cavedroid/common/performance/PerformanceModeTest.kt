package ru.fredboy.cavedroid.common.performance

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PerformanceModeTest {
    @Test
    fun `store release defaults to Firebase and unavailable builds default to OFF`() {
        assertEquals(PerformanceMode.FIREBASE, PerformanceMode.restore(null, firebaseAvailable = true, localAvailable = false))
        assertEquals(PerformanceMode.OFF, PerformanceMode.restore(null, firebaseAvailable = false, localAvailable = false))
        assertEquals(PerformanceMode.OFF, PerformanceMode.restore(null, firebaseAvailable = false, localAvailable = true))
    }

    @Test
    fun `saved opt out survives relaunch and local mode is restored only when supported`() {
        for (available in listOf(false, true)) {
            assertEquals(PerformanceMode.OFF, PerformanceMode.restore("OFF", available, localAvailable = false))
            assertEquals(PerformanceMode.OFF, PerformanceMode.restore("OFF", available, localAvailable = true))
            assertEquals(PerformanceMode.LOCAL, PerformanceMode.restore("LOCAL", available, localAvailable = true))
            assertEquals(PerformanceMode.OFF, PerformanceMode.restore("LOCAL", available, localAvailable = false))
        }
    }

    @Test
    fun `unsupported Firebase and unrecognized preferences cannot enable sending`() {
        assertEquals(PerformanceMode.OFF, PerformanceMode.restore("FIREBASE", firebaseAvailable = false, localAvailable = true))
        assertEquals(PerformanceMode.OFF, PerformanceMode.restore("invalid", firebaseAvailable = true, localAvailable = false))
    }

    @Test
    fun `release toggle skips local and unavailable collectors stay off`() {
        assertEquals(PerformanceMode.FIREBASE, PerformanceMode.OFF.next(localAvailable = false, firebaseAvailable = true))
        assertEquals(PerformanceMode.OFF, PerformanceMode.FIREBASE.next(localAvailable = false, firebaseAvailable = true))
        assertEquals(PerformanceMode.OFF, PerformanceMode.OFF.next(localAvailable = false, firebaseAvailable = false))
    }

    @Test
    fun `debug toggle retains local diagnostics`() {
        assertEquals(PerformanceMode.LOCAL, PerformanceMode.OFF.next(localAvailable = true, firebaseAvailable = false))
        assertEquals(PerformanceMode.OFF, PerformanceMode.LOCAL.next(localAvailable = true, firebaseAvailable = false))
    }
}
