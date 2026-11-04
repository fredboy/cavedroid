package ru.fredboy.cavedroid.game.world.presentation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PoseHistoryTest {
    @Test
    fun `projectile headings cross zero without rotating the long way`() {
        val pose = PoseHistory()
        pose.beforeStep(0f, 0f, 359f)
        pose.afterStep(1f, 0f, 1f)
        assertEquals(0f, pose.sampleHeading(0.5f))
    }

    @Test
    fun `frames between fixed steps advance continuously`() {
        val pose = PoseHistory()
        pose.beforeStep(10f, 20f)
        pose.afterStep(11f, 22f)
        assertEquals(10f, pose.sampleX(0f, 0f))
        assertEquals(10.5f, pose.sampleX(0.5f, 0f))
        assertEquals(21f, pose.sampleY(0.5f))
        assertEquals(11f, pose.sampleX(1f, 0f))
    }

    @Test
    fun `multiple substeps retain only the final pair`() {
        val pose = PoseHistory()
        pose.beforeStep(0f, 0f)
        pose.afterStep(1f, 0f)
        pose.beforeStep(1f, 0f)
        pose.afterStep(2f, 0f)
        assertEquals(1.5f, pose.sampleX(0.5f, 0f))
    }

    @Test
    fun `finite seams interpolate over the short distance in both directions`() {
        val pose = PoseHistory()
        pose.beforeStep(99f, 0f)
        pose.afterStep(1f, 0f)
        assertEquals(0f, pose.sampleX(0.5f, 100f))
        pose.beforeStep(1f, 0f)
        pose.afterStep(99f, 0f)
        assertEquals(100f, pose.sampleX(0.5f, 100f))
    }

    @Test
    fun `reset prevents smearing after a teleport or pause`() {
        val pose = PoseHistory()
        pose.beforeStep(0f, 0f)
        pose.afterStep(1f, 1f)
        assertFalse(pose.matches(100f, 100f, 0f))
        pose.reset(100f, 100f)
        assertTrue(pose.matches(100f, 100f, 0f))
        assertEquals(100f, pose.sampleX(0f, 0f))
        assertEquals(100f, pose.sampleY(0.5f))
    }

    @Test
    fun `30 60 90 and 120 hz presentations have uniform constant velocity`() {
        for (hz in listOf(30, 60, 90, 120)) {
            val pose = PoseHistory()
            var accumulator = 0.0
            var x = 0f
            var previous = 0f
            repeat(hz * 2) { frame ->
                accumulator += 1.0 / hz
                while (accumulator + 1e-12 >= 1.0 / 60) {
                    pose.beforeStep(x, 0f)
                    x += 1f
                    pose.afterStep(x, 0f)
                    accumulator -= 1.0 / 60
                }
                val drawn = if (x == 0f) 0f else pose.sampleX((accumulator * 60).toFloat(), 0f)
                if (frame > 2) assertEquals(60f / hz, drawn - previous, 0.0001f)
                previous = drawn
            }
        }
    }
}
