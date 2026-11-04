package ru.fredboy.cavedroid.gameplay.lighting.bfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class SlicedLightGridTest {
    @Test
    fun `sliced propagation matches an independent flood fill through caves and emitters`() {
        val width = 19
        val height = 32
        val random = Random(14)
        val opaque = BooleanArray(width * height) { random.nextInt(5) == 0 }
        val emission = IntArray(width * height) { if (random.nextInt(30) == 0) random.nextInt(1, 16) else 0 }
        val grid = LightGrid(width, height)
        val build = grid.beginRebuild({ x, y -> opaque[y * width + x] }, { x, y -> emission[y * width + x] })
        var steps = 0
        while (!build.step(Long.MIN_VALUE)) assertTrue(++steps < 1000)
        assertTrue(steps > 1)

        fun reference(sun: Boolean): IntArray {
            val levels = IntArray(width * height)
            val queue = ArrayDeque<Int>()
            for (x in 0 until width) {
                var exposed = true
                for (y in 0 until height) {
                    val i = y * width + x
                    if (opaque[i]) exposed = false
                    levels[i] = if (sun) {
                        if (exposed) 15 else 0
                    } else {
                        emission[i]
                    }
                    if (levels[i] > 0) queue.add(i)
                }
            }
            while (queue.isNotEmpty()) {
                val i = queue.removeFirst()
                val x = i % width
                val y = i / width
                for ((nx, ny) in listOf((x + 1) % width to y, (x + width - 1) % width to y, x to y - 1, x to y + 1)) {
                    if (ny !in 0 until height) continue
                    val n = ny * width + nx
                    if (!opaque[n] && levels[i] - 1 > levels[n]) {
                        levels[n] = levels[i] - 1
                        queue.add(n)
                    }
                }
            }
            return levels
        }
        val sky = reference(true)
        val block = reference(false)
        for (x in 0 until width) {
            for (y in 0 until height) {
                assertEquals(sky[y * width + x], grid.skyAt(x, y), "sky at $x,$y")
                assertEquals(block[y * width + x], grid.blockAt(x, y), "block at $x,$y")
            }
        }
    }

    @Test
    fun `unchanged transient handles do not repeatedly invalidate propagation`() {
        val grid = LightGrid(32, 32)
        grid.setTransientEmitter(1, 4, 4, 13)
        val revision = grid.transientRevision
        repeat(100) { grid.setTransientEmitter(1, 4, 4, 13) }
        assertEquals(revision, grid.transientRevision)
        assertEquals(12, grid.blockAt(5, 4))
    }

    @Test
    fun `primitive queue grows and wraps without losing order`() {
        val queue = LongQueue()
        repeat(1000) { queue.addLast(it.toLong() shl 40) }
        repeat(500) { assertEquals(it.toLong() shl 40, queue.removeFirst()) }
        repeat(1000) { queue.addLast((it + 1000L) shl 40) }
        for (i in 500 until 2000) assertEquals(i.toLong() shl 40, queue.removeFirst())
        assertFalse(queue.isNotEmpty())
    }
}
