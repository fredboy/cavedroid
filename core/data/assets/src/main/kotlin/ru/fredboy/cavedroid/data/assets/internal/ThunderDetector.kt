package ru.fredboy.cavedroid.data.assets.internal

import ru.fredboy.cavedroid.domain.assets.model.ThunderTimeline
import java.io.DataInputStream
import java.io.InputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt

/** Analyzes the loaded WAV once; retains only thunder onsets, not decoded audio. */
internal object ThunderDetector {
    fun analyze(stream: InputStream): ThunderTimeline = DataInputStream(stream.buffered()).use { input ->
        fun fourCC() = ByteArray(4).also(input::readFully).decodeToString()
        fun uint32() = Integer.reverseBytes(input.readInt()).toLong() and 0xffffffffL
        require(fourCC() == "RIFF") { "Expected a RIFF WAV" }
        uint32()
        require(fourCC() == "WAVE") { "Expected WAVE audio" }
        var format = 0
        var channels = 0
        var sampleRate = 0
        var frameBytes = 0
        var bits = 0
        while (true) {
            val chunk = fourCC()
            val size = uint32()
            when (chunk) {
                "fmt " -> {
                    require(size in 16..65536)
                    val bytes = ByteArray(size.toInt()).also(input::readFully)
                    fun u16(offset: Int) = (bytes[offset].toInt() and 255) or ((bytes[offset + 1].toInt() and 255) shl 8)
                    format = u16(0)
                    channels = u16(2)
                    sampleRate = u16(4) or (u16(6) shl 16)
                    frameBytes = u16(12)
                    bits = u16(14)
                    // WAVE_FORMAT_EXTENSIBLE stores the PCM/float subtype in its GUID.
                    if (format == 0xfffe && size >= 40) format = u16(24)
                }
                "data" -> {
                    require(channels in 1..8 && sampleRate in 1000..384000)
                    require((format == 1 && bits in listOf(8, 16, 24, 32)) || (format == 3 && bits == 32)) {
                        "Thunder detection requires PCM or float WAV audio"
                    }
                    require(frameBytes == channels * (bits / 8))
                    return@use analyzeSamples(input, size, format, channels, sampleRate, frameBytes, bits)
                }
                else -> input.skipFully(size)
            }
            if (size % 2L != 0L) input.readByte()
        }
        @Suppress("UNREACHABLE_CODE")
        error("Missing WAV data")
    }

    private fun analyzeSamples(
        input: DataInputStream,
        size: Long,
        format: Int,
        channels: Int,
        sampleRate: Int,
        frameBytes: Int,
        bits: Int,
    ): ThunderTimeline {
        val frames = size / frameBytes
        val windowFrames = sampleRate / 50
        val buffer = ByteArray(windowFrames * frameBytes)
        val low = DoubleArray(channels)
        val low2 = DoubleArray(channels)
        val dc = DoubleArray(channels)
        val lowAlpha = 1 - exp(-2 * PI * 300 / sampleRate)
        val dcAlpha = 1 - exp(-2 * PI * 20 / sampleRate)
        val energies = ArrayList<Double>()
        var remaining = frames
        while (remaining > 0) {
            val count = minOf(remaining, windowFrames.toLong()).toInt()
            input.readFully(buffer, 0, count * frameBytes)
            var energy = 0.0
            for (frame in 0 until count) {
                for (channel in 0 until channels) {
                    val offset = frame * frameBytes + channel * (bits / 8)
                    var raw = 0
                    for (byte in 0 until bits / 8) {
                        raw = raw or ((buffer[offset + byte].toInt() and 255) shl (byte * 8))
                    }
                    val sample = when {
                        format == 3 -> Float.fromBits(raw).toDouble().takeIf { it.isFinite() } ?: 0.0
                        bits == 8 -> (raw - 128) / 128.0
                        else -> (raw shl (32 - bits)) / 2147483648.0
                    }
                    // Two low-pass poles isolate rumble; removing DC avoids offset-induced flashes.
                    low[channel] += lowAlpha * (sample - low[channel])
                    low2[channel] += lowAlpha * (low[channel] - low2[channel])
                    dc[channel] += dcAlpha * (low2[channel] - dc[channel])
                    val rumble = low2[channel] - dc[channel]
                    energy += rumble * rumble
                }
            }
            energies += sqrt(energy / (count * channels))
            remaining -= count
        }
        val duration = frames.toDouble() / sampleRate
        if (energies.isEmpty()) return ThunderTimeline(duration, emptyList())
        val sorted = energies.sorted()
        val noiseFloor = sorted[sorted.size / 4]
        val threshold = max(noiseFloor * 8, sorted.last() * 0.12).coerceAtLeast(0.0001)
        val step = windowFrames.toDouble() / sampleRate
        val onsets = ArrayList<Double>()
        for (i in 0 until energies.size - 2) {
            val preceding = energies.subList(maxOf(0, i - 50), i)
            val baseline = if (preceding.isEmpty()) noiseFloor else preceding.average()
            val time = i * step
            if (energies[i] > threshold && energies[i] > baseline * 4 &&
                (onsets.isEmpty() || time - onsets.last() > 1) &&
                energies[i + 1] > threshold * 0.7 && energies[i + 2] > threshold * 0.7
            ) {
                onsets += time
            }
        }
        return ThunderTimeline(duration, onsets)
    }

    private fun DataInputStream.skipFully(size: Long) {
        var remaining = size
        while (remaining > 0) {
            val skipped = skip(remaining)
            if (skipped == 0L) {
                readByte()
                remaining--
            } else {
                remaining -= skipped
            }
        }
    }
}
