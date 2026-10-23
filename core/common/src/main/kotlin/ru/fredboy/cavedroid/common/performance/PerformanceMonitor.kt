package ru.fredboy.cavedroid.common.performance

/** All gameplay methods are render-thread owned. Background work uses a captured session. */
interface PerformanceMonitor {
    val mode: PerformanceMode get() = PerformanceMode.OFF
    val available: Boolean get() = false
    val supportsFirebase: Boolean get() = false
    val supportsLocal: Boolean get() = false
    val active: Boolean get() = false
    fun setMode(mode: PerformanceMode) {}
    fun setForeground(foreground: Boolean) {}
    fun setGameplay(gameplay: Boolean) {}
    fun setRefreshRate(hz: Double?) {}
    fun updateViewContext(width: Int, height: Int, viewportWidth: Int, viewportHeight: Int, lighting: String, debug: Boolean) {}
    fun setDebugOverlay(enabled: Boolean) {}
    fun setWorldType(type: ru.fredboy.cavedroid.common.model.WorldType) {}
    fun setContext(context: PerformanceContext) {}
    fun beginSession(): PerformanceSession? = null
    fun currentSession(): PerformanceSession? = null
    fun beginReleaseSession() {}
    fun releaseSession(result: PerformanceResult = PerformanceResult.SUCCESS) {}
    fun beginFrame() {}
    fun endFrame() {}
    fun startStage(): Long = NOT_STARTED
    fun endStage(metric: PerformanceMetric, start: Long) {}
    fun addDuration(metric: PerformanceMetric, nanos: Long) {}
    fun incrementGauge(metric: PerformanceGauge, value: Long) {}
    fun gauge(metric: PerformanceGauge, value: Long) {}
    fun beginOperation(
        operation: PerformanceOperation,
        trigger: PerformanceTrigger = PerformanceTrigger.USER,
        startKind: PerformanceStartKind? = null,
    ): PerformanceSpan? = null
    fun menuReady() {}
    fun writtenBytes(bytes: Long) {}
    fun close() {}

    companion object {
        const val NOT_STARTED = Long.MIN_VALUE
    }
}

object NoOpPerformanceMonitor : PerformanceMonitor

data class PerformanceContext(
    val buildCode: Long = 0,
    val worldType: String? = null,
    val lighting: String? = null,
    val surfaceWidth: Int = 0,
    val surfaceHeight: Int = 0,
    val viewportWidth: Int = 0,
    val viewportHeight: Int = 0,
    val debugOverlay: Boolean = false,
    val gpuVendor: String? = null,
    val gpuRenderer: String? = null,
)

inline fun <T> PerformanceMonitor.measure(metric: PerformanceMetric, block: () -> T): T {
    val start = startStage()
    try {
        return block()
    } finally {
        endStage(metric, start)
    }
}

inline fun <T> PerformanceMonitor.operation(operation: PerformanceOperation, block: () -> T): T {
    val span = beginOperation(operation)
    try {
        return block()
    } catch (error: Throwable) {
        span?.fail(error)
        throw error
    } finally {
        span?.finish()
    }
}
