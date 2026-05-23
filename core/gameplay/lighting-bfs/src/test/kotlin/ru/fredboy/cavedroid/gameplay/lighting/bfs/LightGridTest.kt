package ru.fredboy.cavedroid.gameplay.lighting.bfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LightGridTest {

    @Test
    fun `empty world has full sky everywhere`() {
        val world = TestWorld(width = 8, height = 8)
        val grid = world.buildGrid()

        for (x in 0 until world.width) {
            for (y in 0 until world.height) {
                assertEquals(LightGrid.MAX_LEVEL, grid.skyAt(x, y), "sky at ($x,$y) should be 15")
                assertTrue(grid.isSkyExposed(x, y), "($x,$y) should be sky-exposed")
            }
        }
    }

    @Test
    fun `solid floor blocks sky below`() {
        val world = TestWorld(width = 8, height = 16).apply {
            for (x in 0 until width) setOpaque(x, 8)
        }
        val grid = world.buildGrid()

        for (x in 0 until world.width) {
            for (y in 0 until 8) {
                assertEquals(LightGrid.MAX_LEVEL, grid.skyAt(x, y), "above floor at ($x,$y)")
            }
            assertEquals(0, grid.skyAt(x, 8), "in floor cell at ($x,8)")
            for (y in 9 until world.height) {
                assertEquals(0, grid.skyAt(x, y), "below floor at ($x,$y)")
            }
        }
    }

    @Test
    fun `thin pillar does not cast hard skirt - sun flows around it`() {
        val world = TestWorld(width = 12, height = 20)
        for (y in 5..9) world.setOpaque(5, y)

        val grid = world.buildGrid()

        val belowPillar = grid.skyAt(5, 10)
        assertTrue(belowPillar >= 10, "directly below pillar should be lit (got $belowPillar)")

        for (y in 5..9) {
            assertEquals(LightGrid.MAX_LEVEL, grid.skyAt(4, y), "neighbour column at y=$y")
            assertEquals(LightGrid.MAX_LEVEL, grid.skyAt(6, y), "neighbour column at y=$y")
        }
    }

    @Test
    fun `block emitter lights up neighbours and falls off with distance`() {
        val world = TestWorld(width = 64, height = 4)
        world.setEmission(32, 2, level = 14)

        val grid = world.buildGrid()

        assertEquals(14, grid.blockAt(32, 2))
        assertEquals(13, grid.blockAt(31, 2))
        assertEquals(13, grid.blockAt(33, 2))
        assertEquals(12, grid.blockAt(30, 2))
        assertEquals(1, grid.blockAt(45, 2))
        assertEquals(0, grid.blockAt(46, 2))
        assertEquals(0, grid.blockAt(0, 2))
    }

    @Test
    fun `placing then removing an opaque block restores the grid`() {
        val world = TestWorld(width = 8, height = 8)
        val grid = world.buildGrid()
        val before = world.snapshotSky(grid)

        world.setOpaque(3, 4)
        grid.onCellChanged(3, 4, newOpaque = true, newEmission = 0)

        world.clearOpaque(3, 4)
        grid.onCellChanged(3, 4, newOpaque = false, newEmission = 0)

        val after = world.snapshotSky(grid)
        assertArrayEquals2D(before, after)
    }

    @Test
    fun `removing a torch zeroes its propagated light`() {
        val world = TestWorld(width = 32, height = 4)
        world.setEmission(16, 2, level = 14)
        val grid = world.buildGrid()

        assertEquals(13, grid.blockAt(15, 2))
        assertEquals(13, grid.blockAt(17, 2))

        world.clearEmission(16, 2)
        grid.onCellChanged(16, 2, newOpaque = false, newEmission = 0)

        assertEquals(0, grid.blockAt(16, 2))
        assertEquals(0, grid.blockAt(15, 2))
        assertEquals(0, grid.blockAt(17, 2))
        assertEquals(0, grid.blockAt(20, 2))
    }

    @Test
    fun `removing one of two overlapping emitters keeps the brighter source`() {
        val world = TestWorld(width = 64, height = 4)
        world.setEmission(20, 2, 14)
        world.setEmission(24, 2, 14)
        val grid = world.buildGrid()

        assertEquals(13, grid.blockAt(21, 2))
        assertEquals(13, grid.blockAt(23, 2))
        assertEquals(12, grid.blockAt(22, 2))

        world.clearEmission(20, 2)
        grid.onCellChanged(20, 2, newOpaque = false, newEmission = 0)

        assertEquals(14, grid.blockAt(24, 2))
        assertEquals(13, grid.blockAt(23, 2))
        assertEquals(12, grid.blockAt(22, 2))
        assertEquals(11, grid.blockAt(21, 2))
        assertEquals(10, grid.blockAt(20, 2))
    }

    @Test
    fun `furnace transient emitter participates in propagation`() {
        val world = TestWorld(width = 8, height = 4)
        val grid = world.buildGrid()

        grid.setTransientEmitter(id = 1L, x = 4, y = 2, level = 13)

        assertEquals(13, grid.blockAt(4, 2))
        assertEquals(12, grid.blockAt(3, 2))
        assertEquals(12, grid.blockAt(5, 2))

        grid.clearTransientEmitter(id = 1L)
        assertEquals(0, grid.blockAt(4, 2))
        assertEquals(0, grid.blockAt(3, 2))
    }

    @Test
    fun `block emitter does not propagate through opaque cells`() {
        val world = TestWorld(width = 32, height = 8)
        world.setEmission(10, 4, level = 14)
        for (y in 0 until 8) world.setOpaque(11, y)

        val grid = world.buildGrid()

        assertEquals(14, grid.blockAt(10, 4))
        assertEquals(13, grid.blockAt(9, 4))
        assertEquals(0, grid.blockAt(11, 4))
        assertEquals(0, grid.blockAt(12, 4))
    }

    @Test
    fun `closing a vertical shaft from the top darkens the whole shaft`() {
        val world = TestWorld(width = 16, height = 32).apply {
            for (x in 0 until width) {
                for (y in 8 until height) setOpaque(x, y)
            }
            for (y in 8 until 20) clearOpaque(8, y)
        }
        val grid = world.buildGrid()

        assertEquals(LightGrid.MAX_LEVEL, grid.skyAt(8, 19), "shaft bottom should be sky-lit before sealing")

        world.setOpaque(8, 8)
        grid.onCellChanged(8, 8, newOpaque = true, newEmission = 0)

        for (y in 8 until 20) {
            assertEquals(0, grid.skyAt(8, y), "shaft cell ($8,$y) must be dark after sealing the top")
        }
    }

    @Test
    fun `sealing a deep horizontal tunnel drops sky light in the room`() {
        val world = TestWorld(width = 32, height = 32).apply {
            for (x in 0 until width) {
                for (y in 10 until height) setOpaque(x, y)
            }
            for (y in 10 until 25) clearOpaque(15, y)
            for (x in 16 until 22) clearOpaque(x, 24)
        }
        val grid = world.buildGrid()

        assertTrue(grid.skyAt(20, 24) > 0, "deep room should be lit before sealing (got ${grid.skyAt(20, 24)})")

        world.setOpaque(16, 24)
        grid.onCellChanged(16, 24, newOpaque = true, newEmission = 0)

        for (x in 17 until 22) {
            assertEquals(0, grid.skyAt(x, 24), "sealed room cell ($x,24) must be dark")
        }
    }

    @Test
    fun `mob exposure uses heightmap`() {
        val world = TestWorld(width = 8, height = 16).apply {
            for (x in 0 until width) setOpaque(x, 10)
        }
        val grid = world.buildGrid()

        assertTrue(grid.isSkyExposed(3, 5))
        assertFalse(grid.isSkyExposed(3, 10))
        assertFalse(grid.isSkyExposed(3, 11))
    }

    private class TestWorld(val width: Int, val height: Int) {
        private val opaque = Array(width) { BooleanArray(height) }
        private val emission = Array(width) { IntArray(height) }

        fun setOpaque(x: Int, y: Int) {
            opaque[x][y] = true
        }

        fun clearOpaque(x: Int, y: Int) {
            opaque[x][y] = false
        }

        fun setEmission(x: Int, y: Int, level: Int) {
            emission[x][y] = level
        }

        fun clearEmission(x: Int, y: Int) {
            emission[x][y] = 0
        }

        fun isOpaque(x: Int, y: Int): Boolean {
            if (y < 0 || y >= height) return false
            val w = ((x % width) + width) % width
            return opaque[w][y]
        }

        fun emissionAt(x: Int, y: Int): Int {
            if (y < 0 || y >= height) return 0
            val w = ((x % width) + width) % width
            return emission[w][y]
        }

        fun buildGrid(): LightGrid {
            val grid = LightGrid(width, height)
            grid.rebuildAll(
                isOpaque = ::isOpaque,
                blockEmission = ::emissionAt,
            )
            return grid
        }

        fun snapshotSky(grid: LightGrid): Array<IntArray> {
            return Array(width) { x -> IntArray(height) { y -> grid.skyAt(x, y) } }
        }
    }

    private fun assertArrayEquals2D(expected: Array<IntArray>, actual: Array<IntArray>) {
        assertEquals(expected.size, actual.size, "outer size")
        for (x in expected.indices) {
            assertEquals(expected[x].size, actual[x].size, "inner size at $x")
            for (y in expected[x].indices) {
                assertEquals(expected[x][y], actual[x][y], "cell ($x,$y)")
            }
        }
    }
}
