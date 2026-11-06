package ru.fredboy.cavedroid.game.window.waystone

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowsManager

class WaystoneWindowControllerTest {
    private val items = mockk<ItemsRepository>(relaxed = true) {
        every { getItemByKey("waystone").params.name } returns "Waystone"
    }
    private val windows = GameWindowsManager(mockk(relaxed = true), items, mockk(relaxed = true), mockk(relaxed = true))
    private val controller = WaystoneWindowController(windows, items)
    private val model get() = windows.currentSceneWindow as WaystoneWindowModel

    @Test
    fun `block interaction opens the correct coordinates and preserves renamed UI state within the session`() {
        controller.open(-150, 64)
        assertEquals(WaystoneDestination("-150:64", "Waystone", -150, 64), model.source)
        assertFalse(model.canTravel)
        model.rename("Home")
        windows.closeWindow()
        controller.open(-150, 64)
        assertEquals("Home", model.source.name)
        controller.open(2000, 72)
        assertEquals("Waystone", model.source.name)
        assertEquals(2000, model.source.x)
    }
}
