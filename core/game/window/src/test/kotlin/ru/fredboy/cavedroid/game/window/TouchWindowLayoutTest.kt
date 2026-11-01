package ru.fredboy.cavedroid.game.window

import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.game.window.TouchWindowLayout.Kind
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CraftingInventoryWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.game.window.inventory.SurvivalInventoryWindow

class TouchWindowLayoutTest {
    private val items = mockk<ru.fredboy.cavedroid.domain.items.repository.ItemsRepository>(relaxed = true)

    @Test
    fun `all inventory slots remain on the right with activity on the left across screen shapes`() {
        val windows = listOf(
            SurvivalInventoryWindow(items),
            CraftingInventoryWindow(items),
            mockk<AbstractInventoryWindow> { every { type } returns GameWindowType.CHEST },
            mockk<AbstractInventoryWindow> { every { type } returns GameWindowType.FURNACE },
            mockk<AbstractInventoryWindow> { every { type } returns GameWindowType.CREATIVE_INVENTORY },
        ) + CreativeInventoryTabsWindow.Tab.entries.map { tab -> CreativeInventoryTabsWindow(items).apply { selectedTab = tab } }
        for ((width, height) in listOf(640f to 360f, 960f to 540f, 840f to 360f, 320f to 240f, 360f to 640f)) {
            windows.forEach { window ->
                (window as? ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid)?.recipeBookActive = true
                val layout = TouchWindowLayout(width, height, window)
                val inventory = layout.slots.filter { it.kind == Kind.INVENTORY }
                assertEquals((0..35).toSet(), inventory.map { it.index }.toSet())
                layout.slots.forEach { slot ->
                    assertTrue(slot.bounds.width > 0 && slot.bounds.height > 0)
                    assertTrue(Rectangle(0f, 0f, width, height).contains(slot.bounds), "$window: $slot")
                    assertFalse(layout.close.overlaps(slot.bounds))
                    assertFalse(layout.drop.overlaps(slot.bounds))
                    assertEquals(slot, layout.slotAt(slot.bounds.x + slot.bounds.width / 2, slot.bounds.y + slot.bounds.height / 2))
                    if (slot.kind == Kind.INVENTORY) {
                        assertTrue(slot.bounds.x >= layout.right.x)
                    } else {
                        assertTrue(slot.bounds.x + slot.bounds.width <= layout.left.x + layout.left.width)
                    }
                }
                assertNull(layout.slotAt(layout.bounds.x + layout.bounds.width * 287f / 640f, height / 2))
            }
        }
    }

    @Test
    fun `survival uses the same stride three phantom recipe indices as desktop`() {
        val layout = TouchWindowLayout(640f, 360f, SurvivalInventoryWindow(items))
        assertEquals(listOf(0, 1, 3, 4), layout.slots.filter { it.kind == Kind.CRAFT }.map { it.index })
        assertEquals(4, layout.slots.count { it.kind == Kind.ARMOR })
    }

    @Test
    fun `recipe tray fits 24 compact cells above the large paging buttons`() {
        val window = CraftingInventoryWindow(items).apply { recipeBookActive = true }
        val layout = TouchWindowLayout(640f, 360f, window)
        assertEquals(24, layout.slots.count { it.kind == Kind.RECIPE })
        assertEquals(9, layout.slots.count { it.kind == Kind.CRAFT })
        assertFalse(layout.previous.overlaps(layout.next))
    }

    @Test
    fun `fit centers the complete window and keeps slots square on tall and wide viewports`() {
        val window = SurvivalInventoryWindow(items)
        for ((width, height) in listOf(640f to 360f, 1280f to 540f, 360f to 640f)) {
            val layout = TouchWindowLayout(width, height, window)
            val bounds = layout.bounds
            assertEquals(TouchWindowLayout.WIDTH / TouchWindowLayout.HEIGHT, bounds.width / bounds.height, .0001f)
            assertEquals(width / 2, bounds.x + bounds.width / 2, .0001f)
            assertEquals(height / 2, bounds.y + bounds.height / 2, .0001f)
            assertTrue(bounds.x >= 0 && bounds.y >= 0 && bounds.x + bounds.width <= width && bounds.y + bounds.height <= height)
            layout.slots.forEach { slot ->
                assertEquals(slot.bounds.width, slot.bounds.height, .0001f)
                assertTrue(bounds.contains(slot.bounds))
            }
            assertNull(layout.slotAt(0f, 0f))
            assertNull(layout.slotAt(width - 1, height - 1))
        }
    }

    @Test
    fun `buttons use large nonoverlapping hit targets matching the PNG artwork`() {
        val windows = listOf(
            SurvivalInventoryWindow(items).apply { recipeBookActive = true },
            CraftingInventoryWindow(items).apply { recipeBookActive = true },
            CreativeInventoryTabsWindow(items).apply { selectedTab = CreativeInventoryTabsWindow.Tab.Normal },
            CreativeInventoryTabsWindow(items).apply { selectedTab = CreativeInventoryTabsWindow.Tab.Inventory },
        )
        windows.forEach { window ->
            val layout = TouchWindowLayout(TouchWindowLayout.WIDTH, TouchWindowLayout.HEIGHT, window)
            val buttons = (listOf(layout.close, layout.drop, layout.recipeToggle, layout.previous, layout.next, layout.scroll) + layout.tabs).filter { it.width > 0 }
            buttons.forEach { button ->
                assertTrue(button.width >= TouchWindowLayout.MIN_BUTTON_SIZE)
                assertTrue(button.height >= TouchWindowLayout.MIN_BUTTON_SIZE)
                layout.slots.forEach { slot -> assertFalse(button.overlaps(slot.bounds), "$button overlaps $slot") }
            }
        }
    }

    @Test
    fun `activity grids use the former title area while headings stay on the right`() {
        val windows = listOf(
            SurvivalInventoryWindow(items).apply { recipeBookActive = true },
            CraftingInventoryWindow(items).apply { recipeBookActive = true },
        )
        windows.forEach { window ->
            val layout = TouchWindowLayout(TouchWindowLayout.WIDTH, TouchWindowLayout.HEIGHT, window)
            assertTrue(layout.slots.any { it.kind != Kind.INVENTORY && it.bounds.y < layout.headerHeight })
            assertEquals(setOf("inventory", "hotbar"), layout.labels.keys)
            layout.labels.values.forEach { point -> assertTrue(point.first >= layout.right.x) }
            val activity = layout.slots.filter { it.kind == Kind.CRAFT || it.kind == Kind.ARMOR }
            activity.forEach { slot ->
                assertTrue(slot.bounds.width >= 28f)
                assertTrue(slot.bounds.y >= layout.left.y + 4f)
                assertTrue(slot.bounds.y + slot.bounds.height <= layout.recipePanel.y)
                activity.filter { it != slot }.forEach { other -> assertFalse(slot.bounds.overlaps(other.bounds)) }
            }
        }
    }
}
