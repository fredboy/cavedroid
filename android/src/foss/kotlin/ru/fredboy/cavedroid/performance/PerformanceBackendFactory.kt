package ru.fredboy.cavedroid.performance

import android.content.Context
import ru.fredboy.cavedroid.common.performance.PerformanceEvent
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceOperationReport

fun createPerformanceBackend(context: Context): PerformanceBackend = object : PerformanceBackend {
    override val available = false
    override fun enabled(enabled: Boolean) {}
    override fun send(event: PerformanceEvent) {}
    override fun start(id: Long, operation: PerformanceOperation) {}
    override fun finish(report: PerformanceOperationReport) {}
    override fun clear() {}
}
