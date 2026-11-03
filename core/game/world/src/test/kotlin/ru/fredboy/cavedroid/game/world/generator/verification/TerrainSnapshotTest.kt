package ru.fredboy.cavedroid.game.world.generator.verification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException

class TerrainSnapshotTest {
    private val water = TerrainCell("water", 0)
    private val snapshot = TerrainSnapshot("test config", listOf(TerrainColumn(-16, "WINTER", listOf(water), listOf(TerrainCell("ore_coal")))))

    @Test
    fun `binary round trip preserves coordinates blocks fluid states and biomes`() {
        val bytes = ByteArrayOutputStream().also(snapshot::write).toByteArray()
        assertEquals(snapshot, TerrainSnapshot.read(ByteArrayInputStream(bytes)))
        assertThrows(EOFException::class.java) { TerrainSnapshot.read(ByteArrayInputStream(bytes.copyOf(8))) }
    }

    @Test
    fun `comparison detects terrain changes and reports precise locations`() {
        val column = snapshot.columns.single()
        val changes = listOf(
            column.copy(fore = listOf(TerrainCell("lava", 0))),
            column.copy(fore = listOf(water.copy(state = 1))),
            column.copy(biome = "PLAINS"),
            column.copy(back = listOf(TerrainCell("stone"))),
        )
        changes.forEach { changed ->
            val message = snapshot.differences(snapshot.copy(columns = listOf(changed)))
            assertNotNull(message)
            assertTrue(requireNotNull(message).contains("x=-16"))
        }
        assertTrue(requireNotNull(snapshot.differences(snapshot.copy(columns = listOf(changes.first())))).contains("foreground x=-16,y=0"))
    }

    @Test
    fun `historical block aliases compare equal without hiding real changes`() {
        val old = snapshot.copy(columns = listOf(snapshot.columns.single().copy(back = listOf(TerrainCell("coal_ore")))))
        assertNull(old.differences(snapshot))
        assertNotNull(old.differences(snapshot.copy(config = "different seed")))
    }
}
