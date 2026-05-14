package ru.fredboy.cavedroid.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Aggregate of the [CoroutineDispatcher]s used across the app. Injected as a
 * single value from the platform launcher so per-platform wiring is one ctor
 * argument instead of three.
 *
 * - [io] — blocking I/O (file reads/writes, network). JVM: `Dispatchers.IO`.
 *   Web: [GdxMainDispatcher] (browsers are single-threaded).
 * - [background] — CPU-bound work. JVM: `Dispatchers.Default`. Web:
 *   [GdxMainDispatcher].
 * - [main] — libGDX render thread. Always [GdxMainDispatcher].
 */
class AppDispatchers(
    val io: CoroutineDispatcher,
    val background: CoroutineDispatcher,
    val main: CoroutineDispatcher,
)
