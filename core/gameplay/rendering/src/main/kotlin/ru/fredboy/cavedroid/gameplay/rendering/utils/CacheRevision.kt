package ru.fredboy.cavedroid.gameplay.rendering.utils

/** Publication token for a raster assembled over several frames. */
internal class CacheRevision {
    var version = 0L
        private set
    private var published = -1L
    val dirty get() = published != version

    fun invalidate() {
        version++
    }

    fun publish(candidate: Long): Boolean {
        if (candidate != version) return false
        published = candidate
        return true
    }
}
