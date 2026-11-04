package ru.fredboy.cavedroid.gdx.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ShuffledMusicQueueTest {
    @Test
    fun `each cycle plays every track once and never repeats at the boundary`() {
        val tracks = List(7) { "$it.wav" }
        repeat(100) { seed ->
            val queue = ShuffledMusicQueue(tracks, Random(seed))
            var previous: String? = null
            repeat(20) {
                val cycle = List(tracks.size) { queue.next() }
                assertEquals(tracks.toSet(), cycle.toSet())
                assertNotEquals(previous, cycle.first())
                previous = cycle.last()
            }
        }
    }

    @Test
    fun `track order varies with the random seed`() {
        val tracks = List(7) { "$it.wav" }
        val orders = (0..20).map { seed ->
            val queue = ShuffledMusicQueue(tracks, Random(seed))
            List(tracks.size) { queue.next() }
        }
        assertTrue(orders.toSet().size > 1)
    }

    @Test
    fun `empty and single track libraries are supported`() {
        assertNull(ShuffledMusicQueue(emptyList()).next())
        val queue = ShuffledMusicQueue(listOf("only.wav"))
        repeat(3) { assertEquals("only.wav", queue.next()) }
    }

    @Test
    fun `excluding a failed track preserves the remaining cycle and boundary restriction`() {
        val tracks = List(4) { "$it.wav" }
        repeat(100) { seed ->
            val queue = ShuffledMusicQueue(tracks, Random(seed))
            val first = queue.next()
            val failed = requireNotNull(queue.next())
            queue.exclude(failed)
            val rest = List(2) { queue.next() }
            val healthy = tracks.toSet() - failed
            assertEquals(healthy, (listOf(first) + rest).toSet())
            var previous = rest.last()
            repeat(10) {
                val cycle = List(healthy.size) { queue.next() }
                assertEquals(healthy, cycle.toSet())
                assertNotEquals(previous, cycle.first())
                previous = cycle.last()
            }
        }
    }
}
