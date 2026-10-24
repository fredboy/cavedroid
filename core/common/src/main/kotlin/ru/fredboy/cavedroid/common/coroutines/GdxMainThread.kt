package ru.fredboy.cavedroid.common.coroutines

import java.lang.ref.WeakReference

/**
 * Holds a weak reference to the libGDX render (main) thread so off-thread world
 * mutations can be detected. [init] must be called from the application's
 * `create()` and `resume()` callbacks, which libGDX runs on its render thread.
 * Refreshing on resume restores the reference if another Android Activity
 * registered its render thread while this application was paused. This thread
 * also drives `Gdx.app.postRunnable` callbacks and the [GdxMainDispatcher].
 */
object GdxMainThread {
    @Volatile
    private var thread: WeakReference<Thread>? = null

    fun init() {
        thread = WeakReference(Thread.currentThread())
    }

    fun isMainThread(): Boolean {
        val captured = thread ?: return true
        // A cleared reference must not restore the permissive pre-initialization fallback.
        return captured.get() === Thread.currentThread()
    }
}
