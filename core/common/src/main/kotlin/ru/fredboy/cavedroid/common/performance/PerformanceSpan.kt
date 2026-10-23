package ru.fredboy.cavedroid.common.performance

import kotlinx.coroutines.CancellationException

/** Allocated only for operations, never for frame stages. Completion is idempotent. */
class PerformanceSpan internal constructor(
    private val clock: PerformanceClock,
    private val onFinish: (PerformanceSpan, Long) -> Unit,
    val operation: PerformanceOperation? = null,
    val trigger: PerformanceTrigger = PerformanceTrigger.USER,
    val startKind: PerformanceStartKind? = null,
    val heapBefore: Long? = null,
    private val collecting: () -> Boolean = { true },
) {
    val started = clock.nanoTime()
    var result = PerformanceResult.SUCCESS
        private set
    var bytesWritten = 0L
        private set

    @Volatile
    var backgrounded = false
    private var finished = false

    @Synchronized
    fun fail(error: Throwable) {
        result = if (error is CancellationException) PerformanceResult.CANCELLED else PerformanceResult.ERROR
    }

    @Synchronized
    fun addBytes(bytes: Long) {
        bytesWritten += bytes.coerceAtLeast(0)
    }

    @Synchronized
    fun finish(result: PerformanceResult = this.result) {
        if (finished) return
        finished = true
        this.result = result
        onFinish(this, if (collecting()) clock.nanoTime() - started else 0)
    }
}

data class PerformanceOperationReport(
    val id: Long,
    val operation: PerformanceOperation,
    val result: PerformanceResult,
    val durationNanos: Long,
    val context: PerformanceContext,
    val trigger: PerformanceTrigger,
    val startKind: PerformanceStartKind?,
    val bytesWritten: Long,
    val heapBefore: Long?,
    val heapAfter: Long?,
    val backgrounded: Boolean,
    val epoch: Long,
    val chunks: Long? = null,
)

interface PerformanceOutput {
    fun operationStarted(id: Long, operation: PerformanceOperation, epoch: Long) {}
    fun operationFinished(report: PerformanceOperationReport) {}
    fun modeChanged(mode: PerformanceMode, epoch: Long) {}
}

/** A world lifetime, captured by background owners. Never redirects late work to a new world. */
class PerformanceSession internal constructor(
    private val clock: PerformanceClock,
    private val enabled: () -> Boolean,
    private val epoch: () -> Long,
    private val onWindowFull: (PerformanceSession) -> Unit = {},
    private val onReleased: (PerformanceResult) -> Unit,
) {
    internal val durations = Array(PerformanceMetric.entries.size) { DurationHistogram() }
    internal val results = Array(PerformanceMetric.entries.size) { LongArray(PerformanceResult.entries.size) }

    @Volatile
    var publishedChunks = 0L
        private set

    @Volatile
    var writtenChunks = 0L
        private set

    fun chunkPublished() {
        if (enabled()) publishedChunks++
    }

    @Synchronized
    fun chunkWritten() {
        if (enabled()) writtenChunks++
    }

    private var outstanding = 0
    private var releasing = false
    private var releaseResult = PerformanceResult.SUCCESS

    @Synchronized
    fun begin(metric: PerformanceMetric): PerformanceSpan? {
        if (!enabled() || releasing) return null
        outstanding++
        val collectionEpoch = epoch()
        return PerformanceSpan(clock, { span, duration ->
            synchronized(this) {
                if (enabled() && epoch() == collectionEpoch) {
                    if (durations[metric.ordinal].count >= DurationHistogram.MAX_WINDOW_SAMPLES) onWindowFull(this)
                    durations[metric.ordinal].add(duration)
                    results[metric.ordinal][span.result.ordinal]++
                }
                outstanding--
                finishRelease()
            }
        }, collecting = { enabled() && epoch() == collectionEpoch })
    }

    @Synchronized
    internal fun hasSamples(): Boolean = durations.any { it.count > 0 }

    @Synchronized
    internal fun clear() {
        durations.forEach { it.clear() }
        results.forEach { it.fill(0) }
    }

    @Synchronized
    internal fun drain(window: PerformanceWindow) {
        for (i in durations.indices) {
            window.durations[i].merge(durations[i])
            durations[i].clear()
            for (j in results[i].indices) {
                window.results[i][j] += results[i][j]
                results[i][j] = 0
            }
        }
    }

    @Synchronized
    internal fun release(result: PerformanceResult) {
        releasing = true
        releaseResult = result
        finishRelease()
    }

    private fun finishRelease() {
        if (releasing && outstanding == 0) {
            releasing = false
            onReleased(releaseResult)
        }
    }
}

inline fun <T> PerformanceSession?.measure(metric: PerformanceMetric, block: () -> T): T {
    val span = this?.begin(metric)
    try {
        return block()
    } catch (error: Throwable) {
        span?.fail(error)
        throw error
    } finally {
        span?.finish()
    }
}
