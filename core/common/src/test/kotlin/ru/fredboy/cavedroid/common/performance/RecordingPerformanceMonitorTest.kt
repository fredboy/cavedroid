package ru.fredboy.cavedroid.common.performance

import kotlinx.coroutines.CancellationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecordingPerformanceMonitorTest {
    @Test
    fun `release capabilities prevent local collection even outside settings`() {
        for (firebase in listOf(false, true)) {
            val monitor = RecordingPerformanceMonitor(
                clock = { error("Disabled monitor must not read the clock") },
                output = object : PerformanceOutput {},
                supportsFirebase = firebase,
                supportsLocal = false,
            )
            assertEquals(firebase, monitor.available)
            assertThrows(IllegalArgumentException::class.java) { monitor.setMode(PerformanceMode.LOCAL) }
            assertEquals(PerformanceMode.OFF, monitor.mode)
        }
    }

    private class Fixture(windowNanos: Long = 60_000_000_000L) {
        var now = 0L
        var clockReads = 0
        val reports = mutableListOf<PerformanceOperationReport>()
        val monitor = RecordingPerformanceMonitor(
            clock = {
                clockReads++
                now
            },
            output = object : PerformanceOutput {
                override fun operationFinished(report: PerformanceOperationReport) {
                    reports += report
                }
            },
            windowNanos = windowNanos,
        )
        fun start() {
            monitor.setMode(PerformanceMode.LOCAL)
            monitor.beginSession()
            monitor.setGameplay(true)
        }
        fun frame(at: Long, duration: Long = 1_000_000) {
            now = at
            monitor.beginFrame()
            now += duration
            monitor.endFrame()
        }
        fun finish(): PerformanceWindow {
            monitor.setGameplay(false)
            return requireNotNull(monitor.poll())
        }
    }

    @Test
    fun `OFF never reads the clock or produces data`() {
        val f = Fixture()
        f.monitor.beginSession()
        f.monitor.setGameplay(true)
        f.monitor.beginFrame()
        f.monitor.measure(PerformanceMetric.RENDER_TOTAL) { }
        f.monitor.beginOperation(PerformanceOperation.WORLD_LOAD)
        f.monitor.currentSession()?.begin(PerformanceMetric.CHUNK_REQUEST)
        f.monitor.endFrame()
        f.monitor.setForeground(false)
        f.monitor.releaseSession(PerformanceResult.SUCCESS)
        assertEquals(0, f.clockReads)
        assertNull(f.monitor.poll())
        assertTrue(f.reports.isEmpty())
    }

    @Test
    fun `late completion after opt out performs no clock reads`() {
        val f = Fixture()
        f.start()
        val operation = requireNotNull(f.monitor.beginOperation(PerformanceOperation.WORLD_LOAD))
        val background = requireNotNull(f.monitor.currentSession()?.begin(PerformanceMetric.CHUNK_REQUEST))
        f.monitor.setMode(PerformanceMode.OFF)
        val reads = f.clockReads
        operation.finish()
        background.finish()
        assertEquals(reads, f.clockReads)
        assertTrue(f.reports.isEmpty())
    }

    @Test
    fun `warmed gameplay accumulation does not allocate on the JVM thread`() {
        val bean = java.lang.management.ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
        org.junit.jupiter.api.Assumptions.assumeTrue(bean.isThreadAllocatedMemorySupported)
        bean.isThreadAllocatedMemoryEnabled = true
        val threadId = Thread.currentThread().id
        val f = Fixture(windowNanos = Long.MAX_VALUE)
        f.start()
        repeat(30_000) {
            f.monitor.beginFrame()
            f.monitor.measure(PerformanceMetric.PHYSICS) { f.now++ }
            f.monitor.incrementGauge(PerformanceGauge.PHYSICS_STEPS, 1)
            f.monitor.endFrame()
        }
        val before = bean.getThreadAllocatedBytes(threadId)
        repeat(10_000) {
            f.monitor.beginFrame()
            f.monitor.measure(PerformanceMetric.PHYSICS) { f.now++ }
            f.monitor.incrementGauge(PerformanceGauge.PHYSICS_STEPS, 1)
            f.monitor.endFrame()
        }
        val allocated = bean.getThreadAllocatedBytes(threadId) - before
        assertEquals(0L, allocated)
    }

    @Test
    fun `weighted fps uses callback intervals and retains genuine stalls`() {
        val f = Fixture()
        f.start()
        f.frame(0)
        f.frame(10_000_000)
        f.frame(210_000_000)
        val report = f.finish()
        val event = PerformanceEvents.window(report, "test").last()
        assertEquals(2L, event.parameters["interval_n"])
        assertEquals(2_000_000_000.0 / 210_000_000, event.parameters["fps_avg"])
        assertEquals(1L, report.stalls)
    }

    @Test
    fun `pause resume resets interval without manufacturing a stall`() {
        val f = Fixture()
        f.start()
        f.frame(0)
        f.monitor.setForeground(false)
        f.monitor.poll()?.let(f.monitor::recycle)
        f.now = 300_000_000_000
        f.monitor.setForeground(true)
        f.frame(f.now)
        f.frame(f.now + 10_000_000)
        val report = f.finish()
        assertEquals(1L, report.durations[PerformanceMetric.FRAME_INTERVAL.ordinal].count)
        assertEquals(0L, report.stalls)
    }

    @Test
    fun `budget is absent unless refresh rate is known and stall threshold is strict`() {
        val f = Fixture()
        f.start()
        f.frame(0)
        f.frame(100_000_000)
        val unknown = f.finish()
        assertEquals(0L, unknown.stalls)
        assertFalse(PerformanceEvents.window(unknown, "unknown").last().parameters.containsKey("over_budget_n"))
        f.monitor.recycle(unknown)
        f.monitor.setRefreshRate(60.0)
        f.monitor.setGameplay(true)
        f.frame(200_000_000)
        f.frame(220_000_000)
        assertEquals(1L, f.finish().overBudget)
    }

    @Test
    fun `multiple physics steps aggregate once per frame without adding children`() {
        val f = Fixture()
        f.start()
        f.monitor.beginFrame()
        repeat(3) {
            f.monitor.addDuration(PerformanceMetric.PHYSICS, 1000)
            f.monitor.addDuration(PerformanceMetric.BOX2D, 500)
        }
        f.now = 9000
        f.monitor.endFrame()
        val report = f.finish()
        assertEquals(1L, report.durations[PerformanceMetric.PHYSICS.ordinal].count)
        assertEquals(3000L, report.durations[PerformanceMetric.PHYSICS.ordinal].sum)
        assertEquals(1500L, report.durations[PerformanceMetric.BOX2D.ordinal].sum)
        assertEquals(9000L, report.durations[PerformanceMetric.FRAME_CALLBACK.ordinal].sum)
    }

    @Test
    fun `empty partial windows are not queued`() {
        val f = Fixture()
        f.start()
        f.monitor.setGameplay(false)
        assertNull(f.monitor.poll())
    }

    @Test
    fun `queue drops oldest pending report without waiting for worker`() {
        val f = Fixture(windowNanos = 1)
        f.start()
        f.frame(0)
        val held = requireNotNull(f.monitor.poll())
        repeat(5) { f.frame((it + 1) * 10_000_000L) }
        val first = requireNotNull(f.monitor.poll())
        val second = requireNotNull(f.monitor.poll())
        assertEquals(3L, second.droppedReports)
        assertNull(f.monitor.poll())
        f.monitor.recycle(held)
        f.monitor.recycle(first)
        f.monitor.recycle(second)
    }

    @Test
    fun `opt out clears queue and rejects delayed operation completion`() {
        val f = Fixture(windowNanos = 1)
        f.start()
        val span = f.monitor.beginOperation(PerformanceOperation.WORLD_LOAD)
        val background = f.monitor.currentSession()?.begin(PerformanceMetric.CHUNK_REQUEST)
        f.frame(0)
        f.monitor.setMode(PerformanceMode.OFF)
        f.monitor.setMode(PerformanceMode.LOCAL)
        span?.finish()
        background?.finish()
        assertNull(f.monitor.poll())
        assertTrue(f.reports.isEmpty())
        f.frame(10_000_000)
        val report = requireNotNull(f.monitor.poll())
        assertEquals(0L, report.durations[PerformanceMetric.CHUNK_REQUEST.ordinal].count)
    }

    @Test
    fun `unload waits for tracked completion without blocking and late work stays in its world`() {
        val f = Fixture()
        f.start()
        val background = requireNotNull(f.monitor.currentSession()?.begin(PerformanceMetric.CHUNK_REQUEST))
        f.monitor.setGameplay(false)
        f.now = 10
        f.monitor.beginReleaseSession()
        f.now = 20
        f.monitor.releaseSession(PerformanceResult.SUCCESS)
        assertTrue(f.reports.isEmpty())
        f.monitor.beginSession()
        f.now = 40
        background.finish(PerformanceResult.CANCELLED)
        assertEquals(30L, f.reports.single().durationNanos)
        val terminal = requireNotNull(f.monitor.poll())
        assertEquals(1L, terminal.durations[PerformanceMetric.CHUNK_REQUEST.ordinal].count)
        assertEquals(0L, terminal.activeNanos)
        f.monitor.recycle(terminal)
        f.monitor.setGameplay(true)
        f.frame(50)
        assertEquals(0L, f.finish().durations[PerformanceMetric.CHUNK_REQUEST.ordinal].count)
    }

    @Test
    fun `operations finish exactly once with success error and cancellation`() {
        val f = Fixture()
        f.start()
        val errors = listOf(null, IllegalStateException("private"), CancellationException("private"))
        for (error in errors) {
            val span = requireNotNull(f.monitor.beginOperation(PerformanceOperation.WORLD_SAVE))
            f.now += 1000
            if (error != null) span.fail(error)
            span.finish()
            span.finish()
        }
        assertEquals(PerformanceResult.entries, f.reports.map { it.result })
        assertTrue(f.reports.all { it.durationNanos == 1000L })
    }

    @Test
    fun `unavailable and reset GC counters leave gaps and stale memory samples are rejected`() {
        val f = Fixture()
        f.start()
        val epoch = f.monitor.epoch
        val generation = f.monitor.sampleGeneration
        f.monitor.sampleGc(null, null, epoch)
        f.now = 100
        f.monitor.sampleGc(10, 20, epoch)
        f.now = 200
        f.monitor.sampleGc(12, 25, epoch)
        f.now = 300
        f.monitor.sampleGc(1, 1, epoch)
        f.frame(400)
        val report = f.finish()
        assertEquals(2L, report.gcCount)
        assertEquals(5L, report.gcTimeMs)
        assertEquals(100L, report.gcCoverageNanos)
        f.monitor.recycle(report)
        f.monitor.setGameplay(true)
        f.monitor.sample(PerformanceGauge.PROCESS_PSS, 1024, epoch, generation)
        f.frame(1000)
        assertEquals(0L, f.finish().gauges[PerformanceGauge.PROCESS_PSS.ordinal].count)
    }
}
