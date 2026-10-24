package ru.fredboy.cavedroid.common.coroutines

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference

class GdxMainThreadTest {

    private val threadField = GdxMainThread::class.java.getDeclaredField("thread").apply { isAccessible = true }
    private var originalReference: Any? = null

    @BeforeEach
    fun resetRegistration() {
        originalReference = threadField.get(null)
        threadField.set(null, null)
    }

    @AfterEach
    fun restoreRegistration() {
        threadField.set(null, originalReference)
    }

    @Test
    fun `allows access before initialization`() {
        assertTrue(GdxMainThread.isMainThread())
    }

    @Test
    fun `accepts the registered thread`() {
        GdxMainThread.init()

        assertTrue(GdxMainThread.isMainThread())
    }

    @Test
    fun `rejects a different thread`() {
        GdxMainThread.init()
        var accepted = true
        val worker = Thread { accepted = GdxMainThread.isMainThread() }
        worker.start()
        worker.join()

        assertFalse(accepted)
    }

    @Test
    fun `rejects access after the registered thread reference is cleared`() {
        GdxMainThread.init()
        // Clear deterministically instead of depending on garbage collection timing.
        (threadField.get(null) as WeakReference<*>).clear()

        assertFalse(GdxMainThread.isMainThread())
    }

    @Test
    fun `initialization restores access after the reference is cleared`() {
        GdxMainThread.init()
        (threadField.get(null) as WeakReference<*>).clear()

        GdxMainThread.init()

        assertTrue(GdxMainThread.isMainThread())
    }
}
