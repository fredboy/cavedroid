package ru.fredboy.cavedroid.common.performance

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistogramCodecTest {
    @Test
    fun `maximum counters fit thirteen chunks and round trip`() {
        val buckets = IntArray(194) { Int.MAX_VALUE }
        val parts = HistogramCodec.encode(buckets)
        assertEquals(13, parts.size)
        assertTrue(parts.all { it.length <= 100 })
        assertArrayEquals(buckets, HistogramCodec.decode(parts))
    }

    @Test
    fun `sparse and multi-byte counters round trip`() {
        val buckets = IntArray(194) { if (it % 3 == 0) 128 * it else 0 }
        assertArrayEquals(buckets, HistogramCodec.decode(HistogramCodec.encode(buckets)))
    }

    @Test
    fun `truncated data is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { HistogramCodec.decode(listOf("AA==")) }
    }
}
