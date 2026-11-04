package ru.fredboy.cavedroid.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceOperationReport

fun createPerformanceBackend(): PerformanceBackend = FirebasePerformanceBackend()

private class FirebasePerformanceBackend : PerformanceBackend {
    override val available = true
    private val traces = mutableMapOf<Long, Trace>()
    private var collecting = false

    override fun enabled(enabled: Boolean) {
        collecting = enabled
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = enabled
    }

    override fun start(id: Long, operation: PerformanceOperation) {
        if (!collecting || operation !in TRACED || traces.size >= 64) return
        traces[id] = FirebasePerformance.getInstance().newTrace("cd_" + operation.name.lowercase()).also { it.start() }
    }

    override fun finish(report: PerformanceOperationReport) {
        val trace = traces.remove(report.id) ?: return
        if (!collecting) return
        trace.putAttribute("result", report.result.name.lowercase())
        report.context.worldType?.let { trace.putAttribute("world_type", it) }
        report.startKind?.let { trace.putAttribute("start_kind", it.name.lowercase()) }
        trace.putAttribute("trigger", report.trigger.name.lowercase())
        trace.putAttribute("backgrounded", report.backgrounded.toString())
        trace.putMetric("measured_duration_us", report.durationNanos / 1000L)
        trace.putMetric("bytes_written", report.bytesWritten)
        report.chunks?.let { trace.putMetric("chunks_n", it) }
        trace.stop()
    }

    override fun clear() {
        // Do not stop abandoned traces after opt-out: that would submit partial operation data.
        traces.clear()
    }

    companion object {
        private val TRACED = setOf(
            PerformanceOperation.APP_INIT,
            PerformanceOperation.ASSETS_LOAD,
            PerformanceOperation.WORLD_CREATE,
            PerformanceOperation.WORLD_LOAD,
            PerformanceOperation.WORLD_SAVE,
            PerformanceOperation.WORLD_UNLOAD,
        )
    }
}
