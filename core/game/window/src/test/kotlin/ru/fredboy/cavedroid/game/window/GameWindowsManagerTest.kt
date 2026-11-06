package ru.fredboy.cavedroid.game.window

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid

class GameWindowsManagerTest {
    private val manager = GameWindowsManager(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))

    private val craftingWindow get() = manager.currentWindow as AbstractInventoryWindowWithCraftGrid

    @Test
    fun `scene windows and inventory windows are mutually exclusive and close exactly once`() {
        val first = mockk<GameWindowModel>(relaxed = true)
        val second = mockk<GameWindowModel>(relaxed = true)
        manager.openCrafting()
        manager.openWindow(first)
        assertNull(manager.currentWindow)
        assertEquals(GameWindowType.SCENE2D, manager.currentWindowType)
        manager.openWindow(first)
        verify(exactly = 0) { first.onClose() }
        manager.openWindow(second)
        verify(exactly = 1) { first.onClose() }
        manager.openSurvivalInventory()
        verify(exactly = 1) { second.onClose() }
        assertNull(manager.currentSceneWindow)
        assertEquals(GameWindowType.SURVIVAL_INVENTORY, manager.currentWindowType)
        manager.closeWindow()
        manager.closeWindow()
        verify(exactly = 1) { second.onClose() }
    }

    @Test
    fun `recipe visibility survives closes and is shared in both directions`() {
        manager.openSurvivalInventory()
        assertFalse(craftingWindow.recipeBookActive)
        craftingWindow.recipeBookActive = true
        manager.closeWindow()

        manager.openSurvivalInventory()
        assertTrue(craftingWindow.recipeBookActive)
        manager.closeWindow()
        manager.openCrafting()
        assertTrue(craftingWindow.recipeBookActive)
        craftingWindow.recipeBookActive = false
        manager.closeWindow()

        manager.openSurvivalInventory()
        assertFalse(craftingWindow.recipeBookActive)
        manager.closeWindow()
        manager.openCrafting()
        assertFalse(craftingWindow.recipeBookActive)
        craftingWindow.recipeBookActive = true
        manager.closeWindow()
        manager.openSurvivalInventory()
        assertTrue(craftingWindow.recipeBookActive)
    }

    @Test
    fun `closing other windows does not reset recipe visibility`() {
        manager.openCrafting()
        craftingWindow.recipeBookActive = true
        manager.closeWindow()
        manager.openChest(mockk(relaxed = true))
        manager.closeWindow()
        manager.closeWindow()
        manager.openCrafting()
        assertTrue(craftingWindow.recipeBookActive)
    }
}
