package ru.fredboy.cavedroid.common.performance

import com.badlogic.gdx.utils.JsonWriter
import java.io.StringWriter

/** Created exclusively on the reporting worker. Values obey the Analytics scalar schema. */
data class PerformanceEvent(val name: String, val parameters: Map<String, Any>)

object PerformanceEvents {
    fun jsonLine(event: PerformanceEvent): String {
        val buffer = StringWriter()
        JsonWriter(buffer).use { writer ->
            writer.setOutputType(JsonWriter.OutputType.json)
            writer.`object`().set("event", event.name).name("parameters").`object`()
            event.parameters.forEach { (key, value) -> writer.set(key, value) }
            writer.pop().pop()
        }
        return buffer.toString() + "\n"
    }

    fun window(window: PerformanceWindow, reportId: String): List<PerformanceEvent> {
        val events = mutableListOf<PerformanceEvent>()
        fun emit(name: String, params: Map<String, Any>) {
            events += PerformanceEvent(name, mapOf("schema_v" to 1L, "report_id" to reportId) + params)
        }
        val frame = window.durations[PerformanceMetric.FRAME_INTERVAL.ordinal]
        val summary = mutableMapOf<String, Any>(
            "active_ms" to window.activeNanos / 1_000_000L,
            "interval_n" to frame.count,
            "partial" to if (window.partial) 1L else 0L,
            "sample_rate" to 1.0,
            "dropped_reports" to window.droppedReports,
        )
        if (frame.count > 0 && frame.sum > 0) {
            summary["fps_avg"] = frame.count * 1_000_000_000.0 / frame.sum
            summary["fps_p5"] = 1_000_000_000.0 / requireNotNull(frame.percentile(0.95))
            summary["stall_100ms_n"] = window.stalls
            summary["stall_100ms_ratio"] = window.stalls.toDouble() / frame.count
            window.refreshHz?.let {
                summary["over_budget_n"] = window.overBudget
                summary["over_budget_ratio"] = window.overBudget.toDouble() / frame.count
            }
        }
        window.refreshHz?.let {
            summary["target_fps"] = it
            summary["refresh_hz"] = it
            summary["target_source"] = "display_assumed"
        }
        for (metric in PerformanceMetric.entries) {
            val duration = window.durations[metric.ordinal]
            if (duration.count == 0L) continue
            val params = mutableMapOf<String, Any>(
                "metric" to metric.name.lowercase(), "sample_rate" to 1.0, "hist_v" to 1L,
                "n" to duration.count, "sum_us" to duration.sum / 1000L, "max_us" to duration.max / 1000L,
                "avg_us" to duration.sum / duration.count / 1000L,
                "p50_us" to requireNotNull(duration.percentile(0.50)) / 1000L,
                "p95_us" to requireNotNull(duration.percentile(0.95)) / 1000L,
                "p99_us" to requireNotNull(duration.percentile(0.99)) / 1000L,
            )
            HistogramCodec.encode(duration.buckets).forEachIndexed { i, part ->
                params["h" + i.toString().padStart(2, '0')] = part
            }
            emit("cd_perf_duration", params)
            val results = window.results[metric.ordinal]
            if (results.any { it != 0L }) {
                emit(
                    "cd_perf_background",
                    mapOf(
                        "metric" to metric.name.lowercase(),
                        "success_n" to results[0],
                        "error_n" to results[1],
                        "cancelled_n" to results[2],
                    ),
                )
            }
        }
        for (metric in PerformanceGauge.entries) {
            val gauge = window.gauges[metric.ordinal]
            if (gauge.count == 0L) continue
            val memory = metric.ordinal <= PerformanceGauge.PROCESS_PSS.ordinal
            emit(
                if (memory) "cd_perf_memory" else "cd_perf_scene",
                mapOf(
                    "metric" to metric.name.lowercase(),
                    "n" to gauge.count,
                    (if (memory) "avg_bytes" else "avg") to gauge.sum / gauge.count,
                    (if (memory) "max_bytes" else "max") to gauge.max,
                    (if (memory) "last_bytes" else "last") to gauge.last,
                ),
            )
        }
        if (window.gcCoverageNanos > 0) {
            emit(
                "cd_perf_gc",
                mapOf(
                    "gc_count" to window.gcCount,
                    "gc_time_ms" to window.gcTimeMs,
                    "coverage_ms" to window.gcCoverageNanos / 1_000_000L,
                ),
            )
        }
        val context = window.context
        val params = mutableMapOf<String, Any>(
            "build_code" to context.buildCode,
            "surface_w" to context.surfaceWidth.toLong(),
            "surface_h" to context.surfaceHeight.toLong(),
            "viewport_w" to context.viewportWidth.toLong(),
            "viewport_h" to context.viewportHeight.toLong(),
            "debug_overlay" to if (context.debugOverlay) 1L else 0L,
        )
        context.worldType?.let { params["world_type"] = it }
        context.lighting?.let { params["lighting"] = it }
        context.gpuVendor?.let { params["gpu_vendor"] = it.take(100) }
        context.gpuRenderer?.let { params["gpu_renderer"] = it.take(100) }
        emit("cd_perf_context", params)
        summary["event_n"] = events.size.toLong() + 1
        emit("cd_perf_window", summary)
        return events
    }

    fun operation(report: PerformanceOperationReport): PerformanceEvent {
        val params = mutableMapOf<String, Any>(
            "schema_v" to 1L,
            "operation" to report.operation.name.lowercase(),
            "result" to report.result.name.lowercase(),
            "duration_us" to report.durationNanos / 1000L,
            "trigger" to report.trigger.name.lowercase(),
            "bytes_written" to report.bytesWritten,
            "backgrounded" to if (report.backgrounded) 1L else 0L,
        )
        report.chunks?.let { params["chunks_n"] = it }
        report.context.worldType?.let { params["world_type"] = it }
        report.startKind?.let { params["start_kind"] = it.name.lowercase() }
        report.heapBefore?.let { params["heap_before_bytes"] = it }
        report.heapAfter?.let { params["heap_after_bytes"] = it }
        if (report.heapBefore != null && report.heapAfter != null) {
            params["heap_delta_bytes"] = report.heapAfter - report.heapBefore
        }
        if (report.result != PerformanceResult.SUCCESS) params["error_kind"] = report.result.name.lowercase()
        return PerformanceEvent("cd_perf_operation", params)
    }
}
