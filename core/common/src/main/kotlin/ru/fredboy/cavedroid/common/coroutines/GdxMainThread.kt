package ru.fredboy.cavedroid.common.coroutines

/**
 * Holds a reference to the libGDX render (main) thread so off-thread world
 * mutations can be detected. [init] must be called once from the launcher's
 * `create()` callback — libGDX runs `create()` on its render thread, which is
 * the same thread that drives all subsequent `Gdx.app.postRunnable` callbacks
 * and the [GdxMainDispatcher].
 */
object GdxMainThread {
    @Volatile
    private var thread: Thread? = null

    fun init() {
        thread = Thread.currentThread()
    }

    fun isMainThread(): Boolean {
        val captured = thread ?: return true
        return captured === Thread.currentThread()
    }
}
