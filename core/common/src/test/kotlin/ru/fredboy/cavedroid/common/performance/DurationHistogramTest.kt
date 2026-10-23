package ru.fredboy.cavedroid.common.performance

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DurationHistogramTest {
    @Test
    fun `empty histogram has no percentile`() {
        assertNull(DurationHistogram().percentile(0.95))
    }

    @Test
    fun `bucket boundaries include underflow and overflow`() {
        assertEquals(0, DurationHistogram.bucket(999))
        assertEquals(1, DurationHistogram.bucket(1000))
        for (i in 1..192) {
            val boundary = DurationHistogram.upperBound(i)
            assertEquals(i, DurationHistogram.bucket(boundary - 1))
            assertEquals(i + 1, DurationHistogram.bucket(boundary))
        }
        assertEquals(193, DurationHistogram.bucket(Long.MAX_VALUE))
    }

    @Test
    fun `nearest rank estimates bound known distribution`() {
        val histogram = DurationHistogram()
        for (i in 1..100) histogram.add(i * 1_000_000L)
        assertEquals(100L, histogram.count)
        assertEquals(5_050_000_000L, histogram.sum)
        assertEquals(100_000_000L, histogram.max)
        val p95 = requireNotNull(histogram.percentile(0.95))
        assertTrue(p95 >= 95_000_000 && p95 < 95_000_000 * 1.091)
    }

    @Test
    fun `merged histograms equal combined samples`() {
        val left = DurationHistogram()
        val right = DurationHistogram()
        val combined = DurationHistogram()
        repeat(1000) {
            val value = it * 100_001L
            (if (it % 2 == 0) left else right).add(value)
            combined.add(value)
        }
        left.merge(right)
        assertArrayEquals(combined.buckets, left.buckets)
        assertEquals(combined.sum, left.sum)
        assertEquals(combined.percentile(0.99), left.percentile(0.99))
    }

    @Test
    fun `overflow percentile is lower bound while max stays exact`() {
        val histogram = DurationHistogram()
        histogram.add(60_000_000_000)
        assertEquals(16_777_216_000L, histogram.percentile(0.99))
        assertEquals(60_000_000_000L, histogram.max)
    }
}
