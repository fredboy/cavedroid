package ru.fredboy.cavedroid.performance

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import ru.fredboy.cavedroid.common.performance.PerformanceEvent
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceOperationReport

fun createPerformanceBackend(context: Context): PerformanceBackend = FirebasePerformanceBackend(context)

private class FirebasePerformanceBackend(private val context: Context) : PerformanceBackend {
    override val available = true
    private val traces = mutableMapOf<Long, Trace>()
    private var collecting = false

    override fun enabled(enabled: Boolean) {
        collecting = enabled
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enabled)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = enabled
    }

    override fun send(event: PerformanceEvent) {
        if (!collecting) return
        val bundle = Bundle()
        event.parameters.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                else -> error("Invalid performance parameter type")
            }
        }
        FirebaseAnalytics.getInstance(context).logEvent(event.name, bundle)
        if (event.name == "cd_perf_window") {
            val crashlytics = FirebaseCrashlytics.getInstance()
            (event.parameters["fps_avg"] as? Double)?.let { crashlytics.setCustomKey("perf_fps_avg", it) }
            (event.parameters["stall_100ms_ratio"] as? Double)?.let { crashlytics.setCustomKey("perf_stall_ratio", it) }
            (event.parameters["dropped_reports"] as? Long)?.let { crashlytics.setCustomKey("perf_dropped_reports", it) }
        }
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
