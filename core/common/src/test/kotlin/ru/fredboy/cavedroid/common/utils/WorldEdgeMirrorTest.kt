package ru.fredboy.cavedroid.common.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The world wraps horizontally; the renderer mirrors blocks across the seam, but
 * Box2D fixtures and Box2DLights live at absolute coordinates. To make light/shadow
 * work across the seam we maintain mirror copies of edge chunks at +/- worldWidth.
 * These tests pin the predicate that decides which chunks need a mirror, and on
 * which side(s).
 */
class WorldEdgeMirrorTest {

    @Test
    fun `effectiveMirrorBand returns the requested band when it fits in half the world`() {
        assertEquals(80, effectiveMirrorBand(requestedBand = 80, worldWidth = 1024))
    }

    @Test
    fun `effectiveMirrorBand clamps to half world width to prevent overlap`() {
        assertEquals(8, effectiveMirrorBand(requestedBand = 80, worldWidth = 16))
    }

    @Test
    fun `effectiveMirrorBand handles zero world width without dividing improperly`() {
        assertEquals(0, effectiveMirrorBand(requestedBand = 80, worldWidth = 0))
    }

    @Test
    fun `effectiveMirrorBand allows zero requested band`() {
        assertEquals(0, effectiveMirrorBand(requestedBand = 0, worldWidth = 1024))
    }

    @Test
    fun `effectiveMirrorBand rejects negative requested band`() {
        assertThrows(IllegalArgumentException::class.java) {
            effectiveMirrorBand(requestedBand = -1, worldWidth = 1024)
        }
    }

    @Test
    fun `effectiveMirrorBand rejects negative world width`() {
        assertThrows(IllegalArgumentException::class.java) {
            effectiveMirrorBand(requestedBand = 80, worldWidth = -1)
        }
    }

    @Test
    fun `mirrorSidesFor returns +1 only for chunks inside the left band`() {
        assertEquals(listOf(1), mirrorSidesFor(chunkPosition = 0, worldWidth = 1024, band = 80))
        assertEquals(listOf(1), mirrorSidesFor(chunkPosition = 64, worldWidth = 1024, band = 80))
        assertEquals(listOf(1), mirrorSidesFor(chunkPosition = 79, worldWidth = 1024, band = 80))
    }

    @Test
    fun `mirrorSidesFor returns empty for chunks outside both bands`() {
        assertTrue(mirrorSidesFor(chunkPosition = 80, worldWidth = 1024, band = 80).isEmpty())
        assertTrue(mirrorSidesFor(chunkPosition = 512, worldWidth = 1024, band = 80).isEmpty())
        assertTrue(mirrorSidesFor(chunkPosition = 943, worldWidth = 1024, band = 80).isEmpty())
    }

    @Test
    fun `mirrorSidesFor returns -1 only for chunks inside the right band`() {
        assertEquals(listOf(-1), mirrorSidesFor(chunkPosition = 944, worldWidth = 1024, band = 80))
        assertEquals(listOf(-1), mirrorSidesFor(chunkPosition = 1008, worldWidth = 1024, band = 80))
        assertEquals(listOf(-1), mirrorSidesFor(chunkPosition = 1023, worldWidth = 1024, band = 80))
    }

    @Test
    fun `mirrorSidesFor returns both sides for tiny worlds where bands overlap`() {
        // band == worldWidth / 2: every chunk is in both bands. With width=8 and band=4,
        // chunkPosition=4 satisfies both `< band? false` and `>= width-band? true` -> only -1.
        // chunkPosition=3 satisfies `< band? true` and `>= width-band? false` -> only +1.
        // chunkPosition=0 satisfies both with band=8/2=4: `0 < 4` true, `0 >= 8-4=4` false -> only +1.
        // To get both, the band must be > width/2 -- which effectiveMirrorBand prevents.
        // Instead, test that an unclamped caller passing band > width/2 produces both signs:
        assertEquals(listOf(1, -1), mirrorSidesFor(chunkPosition = 2, worldWidth = 4, band = 3))
    }

    @Test
    fun `mirrorSidesFor returns empty when band is zero or negative`() {
        assertTrue(mirrorSidesFor(chunkPosition = 0, worldWidth = 1024, band = 0).isEmpty())
        assertTrue(mirrorSidesFor(chunkPosition = 1023, worldWidth = 1024, band = 0).isEmpty())
        assertTrue(mirrorSidesFor(chunkPosition = 0, worldWidth = 1024, band = -5).isEmpty())
    }

    @Test
    fun `mirrorSidesFor returns empty when world width is zero`() {
        assertTrue(mirrorSidesFor(chunkPosition = 0, worldWidth = 0, band = 80).isEmpty())
    }

    @Test
    fun `mirrorSidesFor at the band boundaries flips inclusive-exclusive correctly`() {
        // band=80 -> left-band is [0, 80), right-band is [width-80, width)
        assertEquals(listOf(1), mirrorSidesFor(chunkPosition = 79, worldWidth = 1024, band = 80))
        assertTrue(mirrorSidesFor(chunkPosition = 80, worldWidth = 1024, band = 80).isEmpty())

        assertTrue(mirrorSidesFor(chunkPosition = 943, worldWidth = 1024, band = 80).isEmpty())
        assertEquals(listOf(-1), mirrorSidesFor(chunkPosition = 944, worldWidth = 1024, band = 80))
    }

    @Test
    fun `mirrorSidesFor with band clamped via effectiveMirrorBand never returns both signs`() {
        val width = 32
        val band = effectiveMirrorBand(requestedBand = 80, worldWidth = width)
        assertEquals(16, band)

        // For every chunk position in the world, the clamped predicate returns at most one side.
        for (chunkPosition in 0 until width) {
            val sides = mirrorSidesFor(chunkPosition, width, band)
            assertTrue(sides.size <= 1, "chunk $chunkPosition produced sides=$sides")
        }
    }
}
