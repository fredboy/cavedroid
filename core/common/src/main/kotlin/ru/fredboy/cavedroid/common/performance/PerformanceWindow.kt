package ru.fredboy.cavedroid.common.performance

class PerformanceWindow {
    val durations = Array(PerformanceMetric.entries.size) { DurationHistogram() }
    val results = Array(PerformanceMetric.entries.size) { LongArray(PerformanceResult.entries.size) }
    val gauges = Array(PerformanceGauge.entries.size) { GaugeSummary() }
    var activeNanos = 0L
    var overBudget = 0L
    var stalls = 0L
    var refreshHz: Double? = null
    var partial = false
    var droppedReports = 0L
    var context = PerformanceContext()
    var gcCount = 0L
    var gcTimeMs = 0L
    var gcCoverageNanos = 0L
    var epoch = 0L

    fun clear() {
        durations.forEach { it.clear() }
        results.forEach { it.fill(0) }
        gauges.forEach { it.clear() }
        activeNanos = 0
        overBudget = 0
        stalls = 0
        gcCount = 0
        gcTimeMs = 0
        gcCoverageNanos = 0
    }

    fun copyFrom(other: PerformanceWindow) {
        clear()
        for (i in durations.indices) durations[i].merge(other.durations[i])
        for (i in results.indices) other.results[i].copyInto(results[i])
        for (i in gauges.indices) gauges[i].merge(other.gauges[i])
        activeNanos = other.activeNanos
        overBudget = other.overBudget
        stalls = other.stalls
        refreshHz = other.refreshHz
        partial = other.partial
        droppedReports = other.droppedReports
        context = other.context
        gcCount = other.gcCount
        gcTimeMs = other.gcTimeMs
        gcCoverageNanos = other.gcCoverageNanos
        epoch = other.epoch
    }
}

class GaugeSummary {
    var count = 0L
        private set
    var sum = 0.0
        private set
    var max = 0L
        private set
    var last = 0L
        private set

    fun add(value: Long) {
        if (value < 0) return
        count++
        sum += value
        max = maxOf(max, value)
        last = value
    }

    fun clear() {
        count = 0
        sum = 0.0
        max = 0
        last = 0
    }

    fun merge(other: GaugeSummary) {
        if (other.count == 0L) return
        count += other.count
        sum += other.sum
        max = maxOf(max, other.max)
        last = other.last
    }
}
