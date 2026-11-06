package ru.fredboy.cavedroid.game.window.waystone

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WaystoneWindowModelTest {
    private val home = WaystoneDestination("home", "Home", 12, 64)
    private val mine = WaystoneDestination("mine", "Mine", -2000, 180)
    private val cabin = WaystoneDestination("cabin", "Cabin", 1400, 64)

    @Test
    fun `source and duplicate destinations are excluded and selection does not travel`() {
        val travelled = mutableListOf<WaystoneDestination>()
        val model = WaystoneWindowModel(home, listOf(home, mine, mine, cabin), travel = travelled::add)
        assertEquals(listOf(mine, cabin), model.destinations)
        assertEquals(mine, model.selected)
        model.select(cabin.id)
        assertTrue(travelled.isEmpty())
        model.teleport()
        assertEquals(listOf(cabin), travelled)
    }

    @Test
    fun `removing selection disables travel without silently selecting a different destination`() {
        val travelled = mutableListOf<WaystoneDestination>()
        val model = WaystoneWindowModel(home, listOf(mine, cabin), travel = travelled::add)
        model.updateDestinations(listOf(cabin))
        assertNull(model.selected)
        assertFalse(model.canTravel)
        model.teleport()
        assertTrue(travelled.isEmpty())
    }

    @Test
    fun `selection follows stable ids when destination details change`() {
        val model = WaystoneWindowModel(home, listOf(mine, cabin))
        val renamed = mine.copy(name = "Deep Mine")
        model.updateDestinations(listOf(cabin, renamed))
        assertEquals(renamed, model.selected)
        assertFalse(model.canTravel)
    }

    @Test
    fun `rename validates and trims before saving and publishing changes`() {
        val names = mutableListOf<String>()
        val model = WaystoneWindowModel(home, saveName = names::add)
        val revision = model.revision
        for (invalid in listOf(" ", "a".repeat(33), "Home\nMine")) assertFalse(model.rename(invalid))
        assertEquals(revision, model.revision)
        assertTrue(names.isEmpty())
        assertTrue(model.rename("  Forest House  "))
        assertEquals(listOf("Forest House"), names)
        assertEquals("Forest House", model.source.name)
        assertTrue(model.revision > revision)
    }
}
