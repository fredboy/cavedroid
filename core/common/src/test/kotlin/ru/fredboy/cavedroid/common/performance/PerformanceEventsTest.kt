package ru.fredboy.cavedroid.common.performance

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PerformanceEventsTest {
    @Test
    fun `all populated event schemas respect Analytics limits and preserve histogram counts`() {
        val window = PerformanceWindow()
        window.context = PerformanceContext(worldType = "infinite", lighting = "bfs", gpuRenderer = "x".repeat(200))
        window.refreshHz = 120.0
        window.gcCoverageNanos = 1000
        window.durations.forEach { histogram ->
            histogram.add(1_000_000)
            histogram.buckets.fill(Int.MAX_VALUE)
        }
        window.gauges.forEach { it.add(1) }
        window.results.forEach { it.fill(1) }
        val events = PerformanceEvents.window(window, "report")
        for (event in events) {
            assertTrue(event.name.matches(Regex("[a-z][a-z0-9_]{0,39}")))
            assertTrue(event.parameters.size <= 25, event.name + " has " + event.parameters.size + " parameters")
            for ((key, value) in event.parameters) {
                assertTrue(key.matches(Regex("[a-z][a-z0-9_]{0,39}")))
                assertTrue(value is Long || value is Double || value is String)
                if (value is String) assertTrue(value.length <= 100)
            }
        }
        val duration = events.first { it.name == "cd_perf_duration" }
        assertEquals(25, duration.parameters.size)
        assertEquals(events.size.toLong(), events.last().parameters["event_n"])
        assertTrue(PerformanceMetric.entries.size <= 64)
        val bytes = events.sumOf { PerformanceEvents.jsonLine(it).toByteArray(Charsets.UTF_8).size }
        assertEquals(95, events.size)
        assertTrue(bytes < 100_000)
        println("Fully populated schema: ${events.size} events, $bytes UTF-8 bytes")
    }

    @Test
    fun `operation schema never includes exception text or world identifiers`() {
        val report = PerformanceOperationReport(
            1, PerformanceOperation.WORLD_LOAD, PerformanceResult.ERROR, 123456,
            PerformanceContext(worldType = "infinite"), PerformanceTrigger.USER,
            PerformanceStartKind.COLD_PROCESS, 42, 10, 20, false, 0,
        )
        val event = PerformanceEvents.operation(report)
        assertEquals(123L, event.parameters["duration_us"])
        assertEquals(10L, event.parameters["heap_delta_bytes"])
        assertEquals("error", event.parameters["error_kind"])
        assertTrue(event.parameters.size <= 25)
        assertTrue(event.parameters.keys.none { it in setOf("seed", "path", "world_id", "exception") })
    }
}
