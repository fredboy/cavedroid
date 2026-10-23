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
    fun `chunk events are batched into one rebuild`() {
        val terrain = Terrain()
        val window = terrain.window()
        terrain.reads = 0

        window.onChunkLoaded(0)
        window.onChunkLoaded(1)
        window.onChunkUnloaded(2)
        assertEquals(0, terrain.reads)

        assertTrue(window.update(0))
        assertEquals(192 * 64, terrain.reads)
        assertFalse(window.update(0))
        assertEquals(192 * 64, terrain.reads)
    }

    @Test
    fun `chunks outside either edge do not rebuild the window`() {
        val terrain = Terrain()
        val window = terrain.window()
        terrain.reads = 0

        window.onChunkLoaded(-7)
        window.onChunkUnloaded(6)

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
        window.grid.setTransientEmitter(1L, 8, 8, 13)
        assertEquals(12, window.grid.blockAt(8, 7))

        window.onChunkLoaded(1)
        window.update(0)

        assertEquals(13, window.grid.blockAt(8, 8))
        assertEquals(12, window.grid.blockAt(8, 7))
        window.grid.clearTransientEmitter(1L)
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
