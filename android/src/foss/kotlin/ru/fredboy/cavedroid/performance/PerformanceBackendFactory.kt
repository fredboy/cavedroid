package ru.fredboy.cavedroid.performance

import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceOperationReport

fun createPerformanceBackend(): PerformanceBackend = object : PerformanceBackend {
    override val available = false
    override fun enabled(enabled: Boolean) {}
    override fun start(id: Long, operation: PerformanceOperation) {}
    override fun finish(report: PerformanceOperationReport) {}
    override fun clear() {}
}
