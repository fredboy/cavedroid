package ru.fredboy.cavedroid.common.performance

/** Render-thread accumulator. Only window handoff and background summaries take locks. */
open class RecordingPerformanceMonitor(
    private val clock: PerformanceClock,
    private val output: PerformanceOutput,
    override val supportsFirebase: Boolean = false,
    private val heap: () -> Long? = { null },
    private val windowNanos: Long = 60_000_000_000L,
    override val supportsLocal: Boolean = true,
) : PerformanceMonitor {
    override val available: Boolean get() = supportsLocal || supportsFirebase

    @Volatile
    final override var mode = PerformanceMode.OFF
        private set

    @Volatile
    var epoch = 0L
        private set

    @Volatile
    final override var active = false
        private set
    private var foreground = true
    private var gameplay = false

    @Volatile
    var sampleGeneration = 0L
        private set
    private var frameStart = PerformanceMonitor.NOT_STARTED
    private var previousFrame = PerformanceMonitor.NOT_STARTED
    private var segmentStart = PerformanceMonitor.NOT_STARTED
    private val frameDurations = LongArray(PerformanceMetric.entries.size)
    private val frameGauges = LongArray(PerformanceGauge.entries.size)
    private val frameGaugeSeen = BooleanArray(PerformanceGauge.entries.size)
    private val frameSeen = BooleanArray(PerformanceMetric.entries.size)
    private val window = PerformanceWindow()
    private val pool = Array(3) { PerformanceWindow() }
    private val free = ArrayDeque<PerformanceWindow>().apply { addAll(pool) }
    private val pending = ArrayDeque<PerformanceWindow>()
    private val handoff = Any()
    private var dropped = 0L
    private var session: PerformanceSession? = null
    private var operationId = 0L
    private var menuSpan: PerformanceSpan? = null
    private var unloading = SessionRelease()
    private val openOperations = mutableSetOf<PerformanceSpan>()

    @Volatile
    private var writeBytes = 0L
    private val samples = PerformanceWindow()
    private var lastGcCount: Long? = null
    private var lastGcTime: Long? = null
    private var lastGcAt = 0L

    override fun setMode(mode: PerformanceMode) {
        if (this.mode == mode) return
        require(mode != PerformanceMode.FIREBASE || supportsFirebase)
        require(mode != PerformanceMode.LOCAL || supportsLocal)
        active = false
        epoch++
        sampleGeneration++
        this.mode = mode
        window.clear()
        synchronized(samples) {
            samples.clear()
            lastGcCount = null
            lastGcTime = null
        }
        synchronized(handoff) {
            while (pending.isNotEmpty()) free.addLast(pending.removeFirst())
        }
        session?.clear()
        synchronized(openOperations) { openOperations.clear() }
        previousFrame = PerformanceMonitor.NOT_STARTED
        frameStart = PerformanceMonitor.NOT_STARTED
        segmentStart = PerformanceMonitor.NOT_STARTED
        output.modeChanged(mode, epoch)
        updateActive()
    }

    override fun setForeground(foreground: Boolean) {
        this.foreground = foreground
        if (!foreground) synchronized(openOperations) { openOperations.forEach { it.backgrounded = true } }
        updateActive()
    }

    override fun setGameplay(gameplay: Boolean) {
        this.gameplay = gameplay
        updateActive()
    }

    private fun updateActive() {
        val next = mode != PerformanceMode.OFF && foreground && gameplay
        if (active == next) return
        if (active) flush(true)
        sampleGeneration++
        active = next
        previousFrame = PerformanceMonitor.NOT_STARTED
        frameStart = PerformanceMonitor.NOT_STARTED
        segmentStart = if (next) clock.nanoTime() else PerformanceMonitor.NOT_STARTED
        synchronized(samples) {
            lastGcCount = null
            lastGcTime = null
        }
    }

    override fun setRefreshRate(hz: Double?) {
        val valid = hz?.takeIf { it.isFinite() && it > 0 }
        if (window.refreshHz == valid) return
        if (active) flush(true)
        resetSampleCoverage()
        window.refreshHz = valid
        previousFrame = PerformanceMonitor.NOT_STARTED
    }

    override fun updateViewContext(width: Int, height: Int, viewportWidth: Int, viewportHeight: Int, lighting: String, debug: Boolean) {
        setContext(
            window.context.copy(
                surfaceWidth = width,
                surfaceHeight = height,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                lighting = lighting,
                debugOverlay = debug,
            ),
        )
    }

    override fun setDebugOverlay(enabled: Boolean) {
        setContext(window.context.copy(debugOverlay = enabled))
    }

    override fun setWorldType(type: ru.fredboy.cavedroid.common.model.WorldType) {
        setContext(window.context.copy(worldType = type.name.lowercase()))
    }

    override fun setContext(context: PerformanceContext) {
        if (window.context == context) return
        if (active) flush(true)
        resetSampleCoverage()
        window.context = context
        unloading.context = context
        previousFrame = PerformanceMonitor.NOT_STARTED
    }

    private fun resetSampleCoverage() {
        sampleGeneration++
        synchronized(samples) {
            lastGcCount = null
            lastGcTime = null
        }
    }

    override fun beginSession(): PerformanceSession {
        window.context = window.context.copy(worldType = null)
        val release = SessionRelease().apply { context = window.context }
        unloading = release
        lateinit var owner: PerformanceSession
        owner = PerformanceSession(
            clock,
            { mode != PerformanceMode.OFF },
            { epoch },
            onWindowFull = { queueBackground(it, release.context, epoch) },
        ) { result ->
            release.span?.finish(result)
            queueBackground(owner, release.context, release.epoch)
        }
        session = owner
        releaseAction = { result -> owner.release(result) }
        return owner
    }

    private class SessionRelease {
        var span: PerformanceSpan? = null
        var context = PerformanceContext()
        var epoch = 0L
    }

    override fun beginReleaseSession() {
        unloading.context = window.context
        unloading.epoch = epoch
        unloading.span = beginOperation(PerformanceOperation.WORLD_UNLOAD)
    }

    private fun queueBackground(owner: PerformanceSession, context: PerformanceContext, generation: Long) {
        if (mode == PerformanceMode.OFF || epoch != generation || !owner.hasSamples()) return
        synchronized(handoff) {
            if (mode == PerformanceMode.OFF || epoch != generation) return
            val snapshot = acquireSnapshot()
            snapshot.clear()
            owner.drain(snapshot)
            snapshot.context = context
            snapshot.epoch = generation
            snapshot.partial = true
            snapshot.refreshHz = null
            snapshot.droppedReports = dropped
            pending.addLast(snapshot)
        }
    }

    @Synchronized
    override fun writtenBytes(bytes: Long) {
        if (mode != PerformanceMode.OFF) writeBytes += bytes
    }

    private var releaseAction: ((PerformanceResult) -> Unit)? = null

    override fun currentSession(): PerformanceSession? = session

    override fun releaseSession(result: PerformanceResult) {
        if (mode != PerformanceMode.OFF) flush(true)
        session = null
        releaseAction?.invoke(result)
        releaseAction = null
    }

    override fun beginFrame() {
        if (!active) return
        val now = clock.nanoTime()
        frameStart = now
        frameGauges.fill(0)
        frameGaugeSeen.fill(false)
        frameDurations.fill(0)
        frameSeen.fill(false)
        if (previousFrame != PerformanceMonitor.NOT_STARTED) {
            val duration = (now - previousFrame).coerceAtLeast(0)
            window.durations[PerformanceMetric.FRAME_INTERVAL.ordinal].add(duration)
            if (duration > 100_000_000L) window.stalls++
            val hz = window.refreshHz
            if (hz != null && duration > 1_000_000_000.0 / hz) window.overBudget++
        }
        previousFrame = now
    }

    override fun endFrame() {
        if (!active || frameStart == PerformanceMonitor.NOT_STARTED) return
        val now = clock.nanoTime()
        addDuration(PerformanceMetric.FRAME_CALLBACK, now - frameStart)
        if (frameSeen[PerformanceMetric.RENDER_TOTAL.ordinal]) {
            val remainder = frameDurations[PerformanceMetric.RENDER_TOTAL.ordinal] -
                frameDurations[PerformanceMetric.RENDER_WORLD.ordinal] -
                frameDurations[PerformanceMetric.RENDER_LIGHTING.ordinal] -
                frameDurations[PerformanceMetric.RENDER_HUD.ordinal]
            addDuration(PerformanceMetric.RENDER_OTHER, remainder.coerceAtLeast(0))
        }
        for (i in frameDurations.indices) {
            if (frameSeen[i]) window.durations[i].add(frameDurations[i])
        }
        for (i in frameGauges.indices) {
            if (frameGaugeSeen[i]) window.gauges[i].add(frameGauges[i])
        }
        frameStart = PerformanceMonitor.NOT_STARTED
        if (now - segmentStart >= windowNanos) {
            flush(false)
        } else if (window.durations[PerformanceMetric.FRAME_CALLBACK.ordinal].count >= DurationHistogram.MAX_WINDOW_SAMPLES) {
            flush(true)
        }
    }

    override fun startStage(): Long = if (active) clock.nanoTime() else PerformanceMonitor.NOT_STARTED

    override fun endStage(metric: PerformanceMetric, start: Long) {
        if (active && start != PerformanceMonitor.NOT_STARTED) addDuration(metric, clock.nanoTime() - start)
    }

    override fun addDuration(metric: PerformanceMetric, nanos: Long) {
        if (!active) return
        if (metric.perFrame) {
            if (frameStart == PerformanceMonitor.NOT_STARTED) return
            frameDurations[metric.ordinal] += nanos.coerceAtLeast(0)
            frameSeen[metric.ordinal] = true
        } else {
            window.durations[metric.ordinal].add(nanos.coerceAtLeast(0))
            if (window.durations[metric.ordinal].count >= DurationHistogram.MAX_WINDOW_SAMPLES) flush(true)
        }
    }

    override fun incrementGauge(metric: PerformanceGauge, value: Long) {
        if (!active || frameStart == PerformanceMonitor.NOT_STARTED) return
        frameGauges[metric.ordinal] += value
        frameGaugeSeen[metric.ordinal] = true
    }

    override fun gauge(metric: PerformanceGauge, value: Long) {
        if (active) window.gauges[metric.ordinal].add(value)
    }

    /** Worker-only memory sampling; never reads or mutates the render-thread buffer. */
    fun sample(metric: PerformanceGauge, value: Long, sampleEpoch: Long, generation: Long = sampleGeneration) {
        synchronized(samples) {
            if (active && epoch == sampleEpoch && sampleGeneration == generation) samples.gauges[metric.ordinal].add(value)
        }
    }

    fun sampleGc(count: Long?, timeMs: Long?, sampleEpoch: Long, generation: Long = sampleGeneration) {
        synchronized(samples) {
            if (!active || epoch != sampleEpoch || sampleGeneration != generation) return
            val now = clock.nanoTime()
            val beforeCount = lastGcCount
            val beforeTime = lastGcTime
            if (count != null &&
                timeMs != null &&
                beforeCount != null &&
                beforeTime != null &&
                count >= beforeCount &&
                timeMs >= beforeTime
            ) {
                samples.gcCount += count - beforeCount
                samples.gcTimeMs += timeMs - beforeTime
                samples.gcCoverageNanos += now - lastGcAt
            }
            lastGcCount = count
            lastGcTime = timeMs
            lastGcAt = now
        }
    }

    private fun flush(partial: Boolean) {
        if (mode == PerformanceMode.OFF) return
        val now = clock.nanoTime()
        if (segmentStart != PerformanceMonitor.NOT_STARTED) window.activeNanos += now - segmentStart
        segmentStart = if (active) now else PerformanceMonitor.NOT_STARTED
        session?.drain(window)
        synchronized(samples) {
            for (i in samples.gauges.indices) window.gauges[i].merge(samples.gauges[i])
            window.gcCount += samples.gcCount
            window.gcTimeMs += samples.gcTimeMs
            window.gcCoverageNanos += samples.gcCoverageNanos
            samples.clear()
        }
        if (window.durations.all { it.count == 0L }) {
            window.clear()
            return
        }
        window.partial = partial
        window.epoch = epoch
        synchronized(handoff) {
            val snapshot = acquireSnapshot()
            window.droppedReports = dropped
            snapshot.copyFrom(window)
            pending.addLast(snapshot)
        }
        window.clear()
    }

    // Caller holds handoff. A single worker owns at most one of the three snapshots.
    private fun acquireSnapshot(): PerformanceWindow {
        return if (pending.size >= 2 || free.isEmpty()) {
            dropped++
            pending.removeFirst()
        } else {
            free.removeFirst()
        }
    }

    fun deliveryDropped() {
        synchronized(handoff) { dropped++ }
    }

    fun poll(): PerformanceWindow? = synchronized(handoff) {
        if (pending.isEmpty()) null else pending.removeFirst()
    }

    fun recycle(snapshot: PerformanceWindow) {
        synchronized(handoff) { free.addLast(snapshot) }
    }

    override fun beginOperation(
        operation: PerformanceOperation,
        trigger: PerformanceTrigger,
        startKind: PerformanceStartKind?,
    ): PerformanceSpan? {
        if (mode == PerformanceMode.OFF) return null
        val generation = epoch
        val id = ++operationId
        val context = window.context
        val initialBytes = writeBytes
        val operationSession = session
        val initialChunks = operationSession?.writtenChunks ?: 0L
        val memory = operation in MEMORY_OPERATIONS
        val before = if (memory) heap() else null
        output.operationStarted(id, operation, generation)
        return PerformanceSpan(clock, { span, duration ->
            synchronized(openOperations) { openOperations.remove(span) }
            if (mode != PerformanceMode.OFF && epoch == generation) {
                output.operationFinished(
                    PerformanceOperationReport(
                        id, operation, span.result, duration,
                        context.copy(
                            worldType = if (operation == PerformanceOperation.WORLD_CREATE || operation == PerformanceOperation.WORLD_LOAD) {
                                window.context.worldType
                            } else {
                                context.worldType
                            },
                        ),
                        trigger, startKind,
                        writeBytes - initialBytes + span.bytesWritten, before, if (memory) heap() else null, span.backgrounded || !foreground, generation,
                        chunks = when (operation) {
                            PerformanceOperation.WORLD_CREATE, PerformanceOperation.WORLD_LOAD -> session?.publishedChunks
                            PerformanceOperation.WORLD_SAVE, PerformanceOperation.WORLD_RELEASE, PerformanceOperation.WORLD_UNLOAD ->
                                operationSession?.let { it.writtenChunks - initialChunks }
                            else -> null
                        },
                    ),
                )
            }
        }, operation, trigger, startKind, before, collecting = { mode != PerformanceMode.OFF && epoch == generation }).also {
            synchronized(openOperations) { openOperations.add(it) }
        }
    }

    fun startMenuReady() {
        menuSpan = beginOperation(PerformanceOperation.MENU_READY)
    }

    override fun menuReady() {
        menuSpan?.finish()
        menuSpan = null
    }

    override fun close() {
        setGameplay(false)
    }

    private companion object {
        val MEMORY_OPERATIONS = setOf(
            PerformanceOperation.WORLD_CREATE,
            PerformanceOperation.WORLD_LOAD,
            PerformanceOperation.WORLD_UNLOAD,
        )
    }
}
