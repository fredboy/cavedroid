package ru.fredboy.cavedroid.game.window

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid

class GameWindowsManagerTest {
    private val manager = GameWindowsManager(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))

    private val craftingWindow get() = manager.currentWindow as AbstractInventoryWindowWithCraftGrid

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
