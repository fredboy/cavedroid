package ru.fredboy.cavedroid.performance

import android.content.Context
import android.os.Debug
import co.touchlab.kermit.Logger
import ru.fredboy.cavedroid.BuildConfig
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.common.performance.PerformanceClock
import ru.fredboy.cavedroid.common.performance.PerformanceEvents
import ru.fredboy.cavedroid.common.performance.PerformanceGauge
import ru.fredboy.cavedroid.common.performance.PerformanceMode
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceOperationReport
import ru.fredboy.cavedroid.common.performance.PerformanceOutput
import ru.fredboy.cavedroid.common.performance.RecordingPerformanceMonitor
import java.io.File
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** Android APIs, JSON and SDK calls run on one worker. The render thread only hands off buffers. */
class AndroidPerformanceMonitor private constructor(
    private val preferences: PreferencesStore,
    private val worker: Worker,
) : RecordingPerformanceMonitor(
    clock = PerformanceClock(System::nanoTime),
    output = worker,
    supportsFirebase = worker.backend.available,
    supportsLocal = BuildConfig.DEBUG,
    heap = { Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() } },
) {
    constructor(context: Context, preferences: PreferencesStore) : this(preferences, Worker(context.applicationContext))

    init {
        worker.monitor = this
        setMode(PerformanceMode.restore(preferences.getPreference(PREFERENCE_KEY), supportsFirebase, supportsLocal))
        if (this.mode == PerformanceMode.OFF) worker.modeChanged(PerformanceMode.OFF, epoch)
    }

    private var startup: ru.fredboy.cavedroid.common.performance.PerformanceSpan? = null

    fun startAndroidStartup(kind: ru.fredboy.cavedroid.common.performance.PerformanceStartKind) {
        startup = beginOperation(PerformanceOperation.ANDROID_STARTUP, startKind = kind)
        startMenuReady()
    }

    override fun menuReady() {
        super.menuReady()
        startup?.finish()
        startup = null
    }

    override fun setContext(context: ru.fredboy.cavedroid.common.performance.PerformanceContext) {
        super.setContext(context.copy(buildCode = BuildConfig.VERSION_CODE.toLong()))
    }

    override fun setMode(mode: PerformanceMode) {
        super.setMode(mode)
        preferences.setPreference(PREFERENCE_KEY, mode.name)
    }

    override fun close() {
        super.close()
        worker.close()
    }

    private class Worker(context: Context) : PerformanceOutput {
        val backend = createPerformanceBackend(context)
        lateinit var monitor: AndroidPerformanceMonitor
        private val executor = ScheduledThreadPoolExecutor(1) { runnable ->
            Thread(runnable, "CaveDroid-performance").apply {
                isDaemon = true
                priority = Thread.MIN_PRIORITY
            }
        }.apply { removeOnCancelPolicy = true }
        private val operations = ArrayBlockingQueue<() -> Unit>(128)
        private val drainScheduled = AtomicBoolean()
        private val closing = AtomicBoolean()
        private val operationsDropped = AtomicBoolean()
        private var schedule: ScheduledFuture<*>? = null
        private val localFile = File(context.filesDir, "performance.jsonl")
        private var lastMemory = 0L
        private var lastPss = 0L
        private var configuredEpoch = -1L

        override fun modeChanged(mode: PerformanceMode, epoch: Long) {
            operations.clear()
            executor.execute {
                if (epoch != monitor.epoch) return@execute
                schedule?.cancel(false)
                backend.enabled(mode == PerformanceMode.FIREBASE)
                backend.clear()
                configuredEpoch = epoch
                lastMemory = 0
                lastPss = 0
                if (mode != PerformanceMode.OFF) {
                    schedule = executor.scheduleWithFixedDelay(::tick, 0, 250, TimeUnit.MILLISECONDS)
                }
            }
        }

        override fun operationStarted(id: Long, operation: PerformanceOperation, epoch: Long) {
            if (monitor.mode != PerformanceMode.FIREBASE) return
            enqueue {
                if (monitor.epoch == epoch && monitor.mode == PerformanceMode.FIREBASE) backend.start(id, operation)
            }
        }

        override fun operationFinished(report: PerformanceOperationReport) {
            enqueue {
                if (monitor.epoch == report.epoch && monitor.mode != PerformanceMode.OFF) {
                    if (monitor.mode == PerformanceMode.FIREBASE) backend.finish(report)
                    send(PerformanceEvents.operation(report), report.epoch)
                }
            }
        }

        private fun enqueue(action: () -> Unit) {
            if (closing.get()) return
            if (!operations.offer(action)) {
                operationsDropped.set(true)
                monitor.deliveryDropped()
                return
            }
            if (drainScheduled.compareAndSet(false, true)) {
                executor.execute {
                    try {
                        if (operationsDropped.getAndSet(false)) backend.clear()
                        while (true) (operations.poll() ?: break).invoke()
                    } catch (error: Exception) {
                        logger.w(error) { "Performance operation delivery failed" }
                    } finally {
                        drainScheduled.set(false)
                    }
                }
            }
        }

        private fun tick() {
            try {
                if (monitor.mode == PerformanceMode.OFF || configuredEpoch != monitor.epoch) return
                while (true) (operations.poll() ?: break).invoke()
                while (true) {
                    val snapshot = monitor.poll() ?: break
                    try {
                        if (snapshot.epoch == monitor.epoch && monitor.mode != PerformanceMode.OFF) {
                            val reportId = UUID.randomUUID().toString()
                            PerformanceEvents.window(snapshot, reportId).forEach { send(it, snapshot.epoch) }
                        }
                    } finally {
                        monitor.recycle(snapshot)
                    }
                }
                if (monitor.active) sampleMemory()
            } catch (error: Exception) {
                logger.w(error) { "Performance worker failed" }
            }
        }

        private fun send(event: ru.fredboy.cavedroid.common.performance.PerformanceEvent, epoch: Long) {
            if (epoch != monitor.epoch) return
            when (monitor.mode) {
                PerformanceMode.OFF -> Unit
                PerformanceMode.FIREBASE -> backend.send(event)
                PerformanceMode.LOCAL -> {
                    if (localFile.length() >= MAX_FILE_BYTES) {
                        val previous = File(localFile.parentFile, "performance.previous.jsonl")
                        previous.delete()
                        localFile.renameTo(previous)
                    }
                    localFile.appendText(PerformanceEvents.jsonLine(event))
                }
            }
        }

        private fun sampleMemory() {
            val now = System.nanoTime()
            val epoch = monitor.epoch
            val generation = monitor.sampleGeneration
            if (now - lastMemory >= 5_000_000_000L) {
                lastMemory = now
                val runtime = Runtime.getRuntime()
                val used = runtime.totalMemory() - runtime.freeMemory()
                monitor.sample(PerformanceGauge.HEAP_USED, used, epoch, generation)
                monitor.sample(PerformanceGauge.HEAP_COMMITTED, runtime.totalMemory(), epoch, generation)
                monitor.sample(PerformanceGauge.HEAP_MAX, runtime.maxMemory(), epoch, generation)
                monitor.sample(PerformanceGauge.HEAP_HEADROOM, runtime.maxMemory() - used, epoch, generation)
                monitor.sample(PerformanceGauge.NATIVE_HEAP, Debug.getNativeHeapAllocatedSize(), epoch, generation)
                monitor.sampleGc(
                    Debug.getRuntimeStat("art.gc.gc-count")?.toLongOrNull(),
                    Debug.getRuntimeStat("art.gc.gc-time")?.toLongOrNull(),
                    epoch,
                    generation,
                )
            }
            if (now - lastPss >= 30_000_000_000L) {
                lastPss = now
                val memory = Debug.MemoryInfo()
                Debug.getMemoryInfo(memory)
                monitor.sample(PerformanceGauge.PROCESS_PSS, memory.totalPss.toLong() * 1024L, epoch, generation)
            }
        }

        fun close() {
            if (!closing.compareAndSet(false, true)) return
            executor.execute {
                schedule?.cancel(false)
                tick()
                backend.clear()
                // Keep the executor able to accept a racing completion drain; it has no periodic work.
                executor.setKeepAliveTime(1, TimeUnit.SECONDS)
                executor.allowCoreThreadTimeOut(true)
            }
        }

        companion object {
            private const val MAX_FILE_BYTES = 1_048_576L
            private val logger = Logger.withTag("Performance")
        }
    }

    companion object {
        const val PREFERENCE_KEY = "performance_mode"
    }
}
