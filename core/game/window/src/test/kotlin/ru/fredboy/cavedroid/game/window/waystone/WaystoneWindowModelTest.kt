package ru.fredboy.cavedroid.game.window.waystone

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravelResult

class WaystoneWindowModelTest {
    private val home = WaystoneDestination("home", "Home", 12, 64)
    private val mine = WaystoneDestination("mine", "Mine", -2000, 180)
    private val cabin = WaystoneDestination("cabin", "Cabin", 1400, 64)

    @Test
    fun `source and duplicate destinations are excluded and selection does not travel`() {
        val travelled = mutableListOf<WaystoneDestination>()
        val model = WaystoneWindowModel(home, listOf(home, mine, mine, cabin), travel = {
            travelled.add(it)
            WaystoneTravelResult.SUCCESS
        })
        assertEquals(listOf(mine, cabin), model.destinations)
        assertEquals(mine, model.selected)
        model.select(cabin.id)
        assertTrue(travelled.isEmpty())
        model.teleport()
        assertTrue(travelled.isEmpty())
        model.update(WaystoneWindowModel.CHARGE_DURATION)
        assertEquals(listOf(cabin), travelled)
    }

    @Test
    fun `removing selection disables travel without silently selecting a different destination`() {
        val travelled = mutableListOf<WaystoneDestination>()
        val model = WaystoneWindowModel(home, listOf(mine, cabin), travel = {
            travelled.add(it)
            WaystoneTravelResult.SUCCESS
        })
        model.updateDestinations(listOf(cabin))
        assertNull(model.selected)
        assertFalse(model.canTravel)
        model.teleport()
        model.update(WaystoneWindowModel.CHARGE_DURATION)
        assertTrue(travelled.isEmpty())
    }

    @Test
    fun `damage cancellation and closing prevent a delayed teleport`() {
        var valid = true
        var count = 0
        val model = WaystoneWindowModel(home, listOf(mine), travel = {
            count++
            WaystoneTravelResult.SUCCESS
        }, canContinue = { valid })
        model.teleport()
        model.update(.25f)
        assertTrue(model.isCharging)
        assertEquals(.25f, model.chargeProgress, .001f)
        valid = false
        model.update(1f)
        assertEquals(WaystoneTravelResult.INTERRUPTED, model.failure)
        assertEquals(0, count)
        valid = true
        model.teleport()
        model.onClose()
        model.update(10f)
        assertEquals(0, count)
    }

    @Test
    fun `removing a charged destination cancels without selecting or travelling somewhere else`() {
        var count = 0
        val model = WaystoneWindowModel(home, listOf(mine, cabin), travel = {
            count++
            WaystoneTravelResult.SUCCESS
        })
        model.teleport()
        model.updateDestinations(listOf(cabin))
        model.update(5f)
        assertNull(model.selected)
        assertEquals(WaystoneTravelResult.DESTINATION_UNAVAILABLE, model.failure)
        assertEquals(0, count)
    }

    @Test
    fun `failed arrival is reported without closing and successful travel runs only once`() {
        var result = WaystoneTravelResult.DESTINATION_BLOCKED
        var count = 0
        val model = WaystoneWindowModel(home, listOf(mine), travel = {
            count++
            result
        })
        model.teleport()
        model.update(1f)
        assertFalse(model.completed)
        assertEquals(result, model.failure)
        result = WaystoneTravelResult.SUCCESS
        model.dismissFailure()
        model.teleport()
        model.update(1f)
        repeat(3) {
            model.update(1f)
            model.teleport()
        }
        assertTrue(model.completed)
        assertEquals(2, count)
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
