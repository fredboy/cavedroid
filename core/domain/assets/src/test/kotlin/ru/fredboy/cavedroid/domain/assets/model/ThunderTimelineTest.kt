package ru.fredboy.cavedroid.domain.assets.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ThunderTimelineTest {
    @Test
    fun `lightning repeats at the same audio position on every loop`() {
        val timeline = ThunderTimeline(10.0, listOf(2.0, 8.0))
        assertEquals(0f, timeline.flashAt(1.99))
        assertEquals(1f, timeline.flashAt(2.02))
        assertEquals(timeline.flashAt(2.02), timeline.flashAt(10002.02))
        assertEquals(0f, timeline.flashAt(2.6))
    }

    @Test
    fun `a flash can fade across the loop boundary`() {
        val timeline = ThunderTimeline(10.0, listOf(9.9))
        assertEquals(0f, timeline.flashAt(0.0))
        assertTrue(timeline.flashAt(10.15) > 0f)
        assertEquals(0f, timeline.flashAt(10.5))
        assertEquals(0f, ThunderTimeline(10.0, emptyList()).flashAt(2.0))
    }
}
