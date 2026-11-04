package ru.fredboy.cavedroid.data.assets.internal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class ThunderDetectorTest {
    @Test
    fun `replacing the waveform moves the detected thunder times`() {
        for (onset in listOf(2.0, 6.0)) {
            val timeline = ThunderDetector.analyze(wav(onset))
            assertEquals(12.0, timeline.durationSeconds, 0.001)
            assertEquals(1, timeline.onsetsSeconds.size)
            assertEquals(onset, timeline.onsetsSeconds.single(), 0.06)
        }
    }

    @Test
    fun `rain without rumble and silence do not trigger lightning`() {
        assertTrue(ThunderDetector.analyze(wav(null)).onsetsSeconds.isEmpty())
        assertTrue(ThunderDetector.analyze(wav(null, silent = true)).onsetsSeconds.isEmpty())
    }

    @Test
    fun `quiet thunder and opposite stereo channels remain detectable`() {
        val timeline = ThunderDetector.analyze(wav(3.0, gain = 0.05, stereo = true))
        assertEquals(1, timeline.onsetsSeconds.size)
        assertEquals(3.0, timeline.onsetsSeconds.single(), 0.06)
    }

    @Test
    fun `detects thunder at the start and separate strikes two seconds apart`() {
        val timeline = ThunderDetector.analyze(wav(0.0, onsets = listOf(0.0, 2.0, 4.0)))
        assertEquals(3, timeline.onsetsSeconds.size)
        timeline.onsetsSeconds.zip(listOf(0.0, 2.0, 4.0)).forEach { (actual, expected) ->
            assertEquals(expected, actual, 0.06)
        }
    }

    @Test
    fun `unsupported and truncated audio are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            ThunderDetector.analyze(ByteArrayInputStream(ByteArray(16)))
        }
        assertThrows(java.io.EOFException::class.java) {
            ThunderDetector.analyze(ByteArrayInputStream(byteArrayOf(1)))
        }
    }

    private fun wav(
        onset: Double?,
        gain: Double = 1.0,
        stereo: Boolean = false,
        silent: Boolean = false,
        onsets: List<Double> = listOfNotNull(onset),
    ): ByteArrayInputStream {
        val sampleRate = 8000
        val channels = if (stereo) 2 else 1
        val frames = sampleRate * 12
        val bytes = ByteArrayOutputStream()
        val output = DataOutputStream(bytes)
        fun int(value: Int) = output.writeInt(Integer.reverseBytes(value))
        fun short(value: Int) = output.writeShort(java.lang.Short.reverseBytes(value.toShort()).toInt())
        output.writeBytes("RIFF")
        int(36 + frames * channels * 2 + 10)
        output.writeBytes("WAVE")
        // Odd-sized metadata chunks must be skipped with their padding byte.
        output.writeBytes("JUNK")
        int(1)
        short(0)
        output.writeBytes("fmt ")
        int(16)
        short(1)
        short(channels)
        int(sampleRate)
        int(sampleRate * channels * 2)
        short(channels * 2)
        short(16)
        output.writeBytes("data")
        int(frames * channels * 2)
        repeat(frames) { frame ->
            val time = frame.toDouble() / sampleRate
            val rain = 0.01 * sin(2 * PI * 1700 * time)
            val thunder = onsets.sumOf { start ->
                if (time >= start) 0.7 * exp(-(time - start) * 4) * sin(2 * PI * 90 * time) else 0.0
            }
            val sample = if (silent) 0 else ((rain + thunder) * gain * 32767).toInt()
            short(sample)
            if (stereo) short(-sample)
        }
        return ByteArrayInputStream(bytes.toByteArray())
    }
}
