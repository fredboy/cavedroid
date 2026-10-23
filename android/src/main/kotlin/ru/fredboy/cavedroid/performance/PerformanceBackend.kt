package ru.fredboy.cavedroid.performance

import ru.fredboy.cavedroid.common.performance.PerformanceEvent
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceOperationReport

interface PerformanceBackend {
    val available: Boolean
    fun enabled(enabled: Boolean)
    fun send(event: PerformanceEvent)
    fun start(id: Long, operation: PerformanceOperation)
    fun finish(report: PerformanceOperationReport)
    fun clear()
}
