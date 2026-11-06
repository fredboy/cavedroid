package ru.fredboy.cavedroid.gameplay.physics.task

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Timer

abstract class BaseGameWorldControllerTask :
    Timer.Task(),
    Disposable {

    @Volatile
    private var isRunning = false

    @Volatile
    private var isShuttingDown = false

    private val lock = Object()

    fun shutdownBlocking() {
        super.cancel()
        isShuttingDown = true
        synchronized(lock) {
            while (isRunning) {
                try {
                    lock.wait()
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
    }

    final override fun cancel() {
        Gdx.app.log(this::class.simpleName, "Blocking cancel() call!")
        shutdownBlocking()
        dispose()
    }

    final override fun run() {
        if (isShuttingDown) {
            return
        }

        synchronized(lock) {
            isRunning = true
            try {
                exec()
            } finally {
                isRunning = false
                lock.notifyAll()
            }
        }
    }

    override fun dispose() {
        // no-op by default
    }

    abstract fun exec()
}
