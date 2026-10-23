package ru.fredboy.cavedroid.common.performance

import kotlin.math.ceil
import kotlin.math.pow

/** 192 log buckets, plus underflow and overflow. Values are nanoseconds. */
class DurationHistogram {
    val buckets = IntArray(BUCKET_COUNT)
    var count = 0L
        private set
    var sum = 0L
        private set
    var max = 0L
        private set

    fun add(nanos: Long) {
        require(nanos >= 0)
        val bucket = bucket(nanos)
        check(buckets[bucket] < Int.MAX_VALUE)
        buckets[bucket]++
        count++
        sum += nanos
        max = maxOf(max, nanos)
    }

    fun clear() {
        buckets.fill(0)
        count = 0
        sum = 0
        max = 0
    }

    fun merge(other: DurationHistogram) {
        for (i in buckets.indices) buckets[i] = Math.addExact(buckets[i], other.buckets[i])
        count += other.count
        sum += other.sum
        max = maxOf(max, other.max)
    }

    /** Nearest rank; upper bound except overflow, which returns its lower bound. */
    fun percentile(quantile: Double): Long? {
        require(quantile > 0 && quantile <= 1)
        if (count == 0L) return null
        val rank = ceil(count * quantile).toLong()
        var cumulative = 0L
        for (i in buckets.indices) {
            cumulative += buckets[i]
            if (cumulative >= rank) return upperBound(i)
        }
        return null
    }

    companion object {
        const val BUCKET_COUNT = 194
        const val MAX_WINDOW_SAMPLES = 1_000_000L
        private val boundaries = LongArray(193) { ceil(1000.0 * 2.0.pow(it / 8.0)).toLong() }

        fun upperBound(bucket: Int): Long = boundaries[minOf(bucket, 192)]

        fun bucket(nanos: Long): Int {
            var low = 0
            var high = boundaries.size
            while (low < high) {
                val mid = (low + high) ushr 1
                if (nanos < boundaries[mid]) high = mid else low = mid + 1
            }
            return low
        }
    }
}
