package ru.fredboy.cavedroid.common.performance

import kotlin.io.encoding.Base64

/** Worker-only unsigned varints, Base64 split into at most thirteen 100-character parts. */
object HistogramCodec {
    fun encode(buckets: IntArray): List<String> {
        require(buckets.size == DurationHistogram.BUCKET_COUNT)
        val bytes = ByteArray(buckets.size * 5)
        var size = 0
        for (count in buckets) {
            require(count >= 0)
            var value = count
            do {
                val next = value ushr 7
                bytes[size++] = ((value and 127) or if (next != 0) 128 else 0).toByte()
                value = next
            } while (value != 0)
        }
        return Base64.encode(bytes, 0, size).chunked(100)
    }

    fun decode(parts: List<String>): IntArray {
        require(parts.size <= 13 && parts.all { it.length <= 100 })
        val bytes = Base64.decode(parts.joinToString(""))
        val counts = IntArray(DurationHistogram.BUCKET_COUNT)
        var offset = 0
        for (i in counts.indices) {
            var value = 0L
            var shift = 0
            do {
                require(offset < bytes.size && shift <= 28)
                val byte = bytes[offset++].toInt() and 255
                value = value or ((byte and 127).toLong() shl shift)
                shift += 7
            } while (byte and 128 != 0)
            require(value <= Int.MAX_VALUE)
            counts[i] = value.toInt()
        }
        require(offset == bytes.size)
        return counts
    }
}
