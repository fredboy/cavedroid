package ru.fredboy.cavedroid.gameplay.lighting.bfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator

class LightingWindowTest {

    @Test
    fun `late chunk load restores shadows without camera movement`() {
        val terrain = Terrain()
        val window = terrain.window()
        window.update(96)
        assertEquals(1f, window.grid.effective(120, 32, 1f))

        terrain.loaded.add(7)
        window.onChunkLoaded(7)
        assertTrue(window.update(96))

        assertTrue(window.grid.isOpaqueAt(120, 32))
        assertEquals(0f, window.grid.effective(120, 32, 1f))
    }

    @Test
    fun `late chunk load at negative coordinates restores shadows`() {
        val terrain = Terrain()
        val window = terrain.window()
        window.update(-96)
        assertEquals(1f, window.grid.effective(-120, 32, 1f))

        terrain.loaded.add(-8)
        window.onChunkLoaded(-8)
        window.update(-96)

        assertTrue(window.grid.isOpaqueAt(-120, 32))
        assertEquals(0f, window.grid.effective(-120, 32, 1f))
    }

    @Test
    fun `unloading a chunk removes its cached opacity`() {
        val terrain = Terrain()
        val window = terrain.window()
        assertTrue(window.grid.isOpaqueAt(8, 32))

        terrain.loaded.remove(0)
        window.onChunkUnloaded(0)
        window.update(0)

        assertFalse(window.grid.isOpaqueAt(8, 32))
        assertEquals(1f, window.grid.effective(8, 32, 1f))
    }

    @Test
    fun `chunk events refresh only affected strips and are batched`() {
        val terrain = Terrain()
        val window = terrain.window()
        terrain.reads = 0

        window.onChunkLoaded(0)
        window.onChunkLoaded(1)
        window.onChunkUnloaded(2)
        assertEquals(0, terrain.reads)

        assertTrue(window.update(0))
        assertTrue(terrain.reads > 0)
        val reads = terrain.reads
        assertFalse(window.update(0))
        assertEquals(reads, terrain.reads)
    }

    @Test
    fun `chunks outside either edge do not rebuild the window`() {
        val terrain = Terrain()
        val window = terrain.window()
        terrain.reads = 0

        window.onChunkLoaded(-8)
        window.onChunkUnloaded(7)

        assertFalse(window.update(0))
        assertEquals(0, terrain.reads)
    }

    @Test
    fun `partially overlapping chunks invalidate a window not aligned to chunk boundaries`() {
        val terrain = Terrain()
        val window = terrain.window()
        window.update(33)

        window.onChunkLoaded(-4)
        assertTrue(window.update(33))
        window.onChunkUnloaded(8)
        assertTrue(window.update(33))
    }

    @Test
    fun `camera recentering still reads current terrain`() {
        val terrain = Terrain()
        val window = terrain.window()
        terrain.loaded.addAll(58..70)

        assertTrue(window.update(1024))

        assertTrue(window.contains(1024))
        assertFalse(window.contains(0))
        assertEquals(0f, window.grid.effective(1024, 32, 1f))
        assertFalse(window.update(1055))
        assertTrue(window.update(1056))
    }

    @Test
    fun `terrain refresh preserves transient light inside an opaque block`() {
        val terrain = Terrain()
        val window = terrain.window()
        window.setEmitter(1L, 8, 8, 13)
        assertEquals(12, window.grid.blockAt(8, 7))

        window.onChunkLoaded(1)
        window.update(0)

        assertEquals(13, window.grid.blockAt(8, 8))
        assertEquals(12, window.grid.blockAt(8, 7))
        window.setEmitter(1L, 8, 8, 0)
        assertEquals(0, window.grid.blockAt(8, 7))
    }

    @Test
    fun `late chunk load adds block emission`() {
        val terrain = Terrain()
        val window = terrain.window()
        window.update(96)
        assertEquals(0, window.grid.blockAt(120, 7))

        terrain.loaded.add(7)
        terrain.emitters.add(120 to 8)
        window.onChunkLoaded(7)
        window.update(96)

        assertEquals(12, window.grid.blockAt(120, 7))
    }

    @Test
    fun `partial rebuild retains the published grid and superseded terrain is rejected`() {
        val terrain = Terrain()
        val window = terrain.window()
        window.update(64, 0)
        assertFalse(window.covers(112, 127))
        terrain.loaded.add(7)
        window.onChunkLoaded(7)
        var steps = 0
        while (!window.covers(112, 127)) {
            window.update(64, 0)
            assertTrue(++steps < 1000)
        }
        assertTrue(window.grid.isOpaqueAt(120, 32))
    }

    @Test
    fun `emitter outside the old window survives recentering and disposal during a build`() {
        val terrain = Terrain()
        val window = terrain.window()
        window.setEmitter(1L, 120, 7, 13)
        assertEquals(0, window.grid.blockAt(120, 7))
        window.update(64)
        assertEquals(13, window.grid.blockAt(120, 7))
        window.update(96, 0)
        window.setEmitter(1L, 120, 7, 0)
        var steps = 0
        while (!window.covers(120, 120) || window.grid.blockAt(120, 7) != 0) {
            window.update(96, 0)
            assertTrue(++steps < 1000)
        }
        assertEquals(0, window.grid.blockAt(120, 7))
    }

    @Test
    fun `moving emitters and camera do not restart visible work`() {
        val reads = mutableMapOf<Pair<Int, Int>, Int>()
        val window = LightingWindow(
            height = 64,
            isOpaque = { x, y ->
                val cell = x to y
                reads[cell] = (reads[cell] ?: 0) + 1
                false
            },
            blockEmission = { _, _ -> 0 },
        )
        reads.clear()
        var steps = 0
        do {
            window.setEmitter(1, 116 + steps % 4, 20, 13)
            window.update(if (steps < 3) 64 else 96, 0, 112, 127)
            assertTrue(++steps < 100)
        } while (!window.covers(112, 127))

        assertEquals(1, reads[97 to 0], "The visible strip's halo must be scanned only once")
        assertEquals(null, reads[-15 to 0], "Offscreen work must wait for the visible strip")
        assertEquals(13, window.grid.blockAt(116 + (steps - 1) % 4, 20))
    }

    @Test
    fun `chunk changes repair an in-progress strip instead of scanning it again`() {
        val reads = mutableMapOf<Pair<Int, Int>, Int>()
        var loaded = false
        val window = LightingWindow(
            height = 64,
            isOpaque = { x, y ->
                val cell = x to y
                reads[cell] = (reads[cell] ?: 0) + 1
                loaded && x in 112..127 && y >= 8
            },
            blockEmission = { _, _ -> 0 },
        )
        reads.clear()
        window.update(64, 0, 112, 127)
        repeat(4) {
            window.onChunkLoaded(7)
            window.update(64, 0, 112, 127)
        }
        loaded = true
        window.onChunkLoaded(7)
        var steps = 0
        while (!window.covers(112, 127)) {
            window.update(64, 0, 112, 127)
            assertTrue(++steps < 100)
        }
        assertEquals(1, reads[97 to 0], "Chunk events must not rescan unchanged columns")
        assertTrue(window.grid.isOpaqueAt(120, 32))
        assertEquals(0f, window.grid.effective(120, 32, 1f))
    }

    @Test
    fun `moving a visible emitter does not schedule another terrain rebuild`() {
        val terrain = Terrain()
        val window = terrain.window()
        terrain.reads = 0
        repeat(20) {
            window.setEmitter(1, it, 7, 13)
            assertFalse(window.update(0))
            assertEquals(13, window.grid.blockAt(it, 7))
        }
        assertEquals(0, terrain.reads)
    }

    @Test
    fun `light near a window edge does not wrap to the other edge`() {
        val window = LightingWindow(32, { _, _ -> false }, { _, _ -> 0 })
        window.setEmitter(1, 95, 16, 13)
        assertEquals(0, window.grid.blockAt(-96, 16))
        assertEquals(0, window.grid.blockAt(96, 16))
        window.setEmitter(1, 100, 16, 13)
        window.update(0)
        assertEquals(8, window.grid.blockAt(95, 16))
        window.setEmitter(1, 100, 16, 0)
        window.update(0)
        assertEquals(0, window.grid.blockAt(95, 16))
    }

    @Test
    fun `strip halos match a full rebuild across seams and negative coordinates`() {
        fun opaque(x: Int, y: Int): Boolean = y >= 10 && (Math.floorMod(x * 17 + y * 13, 23) < 7 || y == 40)
        fun emission(x: Int, y: Int): Int = if (y == 15 && Math.floorMod(x, 17) == 0) 13 else 0
        val window = LightingWindow(48, ::opaque, ::emission)
        val reference = LightGrid(512, 48, -256)
        reference.rebuildAll({ x, y -> opaque(x - 256, y) }, { x, y -> emission(x - 256, y) })
        for (center in listOf(0, -128, 128)) {
            window.update(center)
            for (x in center - 96 until center + 96) {
                for (y in 0 until 48) {
                    assertEquals(reference.skyAt(x, y), window.grid.skyAt(x, y), "sky at $x,$y")
                    assertEquals(reference.blockAt(x, y), window.grid.blockAt(x, y), "block at $x,$y")
                }
            }
        }
    }

    private class Terrain {
        val loaded = (-6..6).toMutableSet()
        val emitters = mutableSetOf<Pair<Int, Int>>()
        var reads = 0

        fun window() = LightingWindow(
            height = 64,
            isOpaque = { x, y ->
                reads++
                Math.floorDiv(x, ChunkGenerator.CHUNK_W) in loaded && y >= 8
            },
            blockEmission = { x, y ->
                if (Math.floorDiv(x, ChunkGenerator.CHUNK_W) in loaded && (x to y) in emitters) 13 else 0
            },
        )
    }
}
