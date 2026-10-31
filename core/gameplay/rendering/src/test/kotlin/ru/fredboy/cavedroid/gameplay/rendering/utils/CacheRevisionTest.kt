package ru.fredboy.cavedroid.gameplay.rendering.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CacheRevisionTest {
    @Test
    fun `edits or chunk reloads invalidate a partially rasterized image`() {
        val revision = CacheRevision()
        val stale = revision.version
        revision.invalidate()
        assertFalse(revision.publish(stale))
        assertTrue(revision.dirty)
        assertTrue(revision.publish(revision.version))
        assertFalse(revision.dirty)
    }

    @Test
    fun `multiple edits cannot let an older completion mark the cache clean`() {
        val revision = CacheRevision()
        assertTrue(revision.publish(revision.version))
        revision.invalidate()
        val firstEdit = revision.version
        revision.invalidate()
        revision.invalidate()
        assertFalse(revision.publish(firstEdit))
        assertTrue(revision.dirty)
        assertTrue(revision.publish(revision.version))
        assertFalse(revision.publish(firstEdit))
        assertFalse(revision.dirty)
    }
}
