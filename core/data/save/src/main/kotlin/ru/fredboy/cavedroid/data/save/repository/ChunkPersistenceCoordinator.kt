package ru.fredboy.cavedroid.data.save.repository

import ru.fredboy.cavedroid.domain.save.PreparedSave
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChunkPersistenceCoordinator @Inject constructor() {
    private class FileState {
        var references = 0
        var nextRevision = 0L
        var persistedRevision = 0L
    }

    private val files = mutableMapOf<String, FileState>()

    @Synchronized
    private fun acquire(key: String): FileState = files.getOrPut(key) { FileState() }.also { it.references++ }

    @Synchronized
    private fun release(key: String, state: FileState) {
        state.references--
        if (state.references == 0) files.remove(key)
    }

    fun prepare(key: String, encode: () -> ByteArray?, persist: (ByteArray?) -> Unit): PreparedSave {
        val state = acquire(key)
        val revision = synchronized(state) { ++state.nextRevision }
        return object : PreparedSave {
            private var finished = false

            override fun write() {
                val bytes = encode()
                synchronized(state) {
                    if (revision <= state.persistedRevision) return
                    persist(bytes)
                    state.persistedRevision = revision
                }
            }

            override fun finish() {
                if (finished) return
                finished = true
                release(key, state)
            }
        }
    }

    fun <T> read(key: String, read: () -> T): T {
        val state = acquire(key)
        try {
            return synchronized(state) { read() }
        } finally {
            release(key, state)
        }
    }
}
