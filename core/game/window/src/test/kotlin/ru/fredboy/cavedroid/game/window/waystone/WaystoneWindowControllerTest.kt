package ru.fredboy.cavedroid.game.window.waystone

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.waystone.Waystone
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravel
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravelResult
import ru.fredboy.cavedroid.game.window.GameWindowsManager

class WaystoneWindowControllerTest {
    private val items = mockk<ItemsRepository>(relaxed = true) {
        every { getItemByKey("waystone").params.name } returns "Waystone"
    }
    private val windows = GameWindowsManager(mockk(relaxed = true), items, mockk(relaxed = true), mockk(relaxed = true))
    private val names = mutableMapOf<Pair<Int, Int>, String>()
    private var health = 20
    private val destination = Waystone(2000, 72, Layer.BACKGROUND, "Mine")
    private val travel = mockk<WaystoneTravel> {
        every { activate(any(), any(), any()) } answers { Waystone(firstArg(), secondArg(), Layer.FOREGROUND, names[firstArg<Int>() to secondArg<Int>()] ?: thirdArg()) }
        every { rename(any(), any()) } answers {
            names[-150 to 64] = secondArg()
            true
        }
        every { destinations(any()) } returns listOf(destination)
        every { distance(any(), any()) } returns 2150L
        every { canUse(any()) } returns true
        every { playerHealth } answers { health }
        every { teleport(any(), any()) } returns WaystoneTravelResult.SUCCESS
    }
    private val controller = WaystoneWindowController(windows, items, travel)
    private val model get() = windows.currentSceneWindow as WaystoneWindowModel

    @Test
    fun `block interaction opens the correct coordinates and preserves renamed UI state within the session`() {
        controller.open(-150, 64)
        assertEquals(WaystoneDestination("0:-150:64", "Waystone", -150, 64), model.source)
        model.rename("Home")
        windows.closeWindow()
        controller.open(-150, 64)
        assertEquals("Home", model.source.name)
        controller.open(2000, 72)
        assertEquals("Waystone", model.source.name)
        assertEquals(2000, model.source.x)
    }

    @Test
    fun `health lost during charging cancels without calling the travel backend`() {
        controller.open(-150, 64)
        model.teleport()
        model.update(.5f)
        health--
        model.update(.6f)
        assertFalse(model.isCharging)
        assertEquals(WaystoneTravelResult.INTERRUPTED, model.failure)
        verify(exactly = 0) { travel.teleport(any(), any()) }
    }
}
