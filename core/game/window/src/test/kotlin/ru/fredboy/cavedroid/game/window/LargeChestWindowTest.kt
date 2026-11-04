package ru.fredboy.cavedroid.game.window

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.game.window.inventory.ChestInventoryWindow

class LargeChestWindowTest {
    @Test
    fun `slot replacements and item mutations write through to the original halves`() {
        val left = Chest(mockk(relaxed = true))
        val right = Chest(mockk(relaxed = true))
        val window = ChestInventoryWindow(left, right)
        assertEquals(54, window.items.size)
        for (index in window.items.indices) {
            val item = mockk<InventoryItem>()
            window.items[index] = item
            assertSame(item, (if (index < 27) left else right).items[index % 27])
        }
        val replacement = mockk<InventoryItem>()
        right.items[0] = replacement
        assertSame(replacement, window.items[27])
        assertEquals(left.items + right.items, window.items.toList())
        val restoredLeft = Chest(mockk(relaxed = true), left.items.toList())
        val restoredRight = Chest(mockk(relaxed = true), right.items.toList())
        assertEquals(window.items, ChestInventoryWindow(restoredLeft, restoredRight).items)
    }

    @Test
    fun `large chest fits every slot using inventory sized cells and matching touch targets`() {
        val window = ChestInventoryWindow(Chest(mockk(relaxed = true)), Chest(mockk(relaxed = true)))
        for ((width, height) in listOf(640f to 368f, 960f to 540f, 360f to 640f)) {
            val layout = TouchWindowLayout(width, height, window)
            assertEquals("chest_large", layout.background)
            val chest = layout.slots.filter { it.kind == TouchWindowLayout.Kind.CHEST }
            val inventory = layout.slots.filter { it.kind == TouchWindowLayout.Kind.INVENTORY }
            assertEquals((0..53).toList(), chest.map { it.index })
            assertEquals((0..35).toSet(), inventory.map { it.index }.toSet())
            chest.forEach {
                assertEquals(inventory.first().bounds.width, it.bounds.width)
                assertEquals(inventory.first().bounds.height, it.bounds.height)
                assertTrue(layout.left.contains(it.bounds))
            }
            layout.slots.forEach { slot ->
                assertTrue(layout.bounds.contains(slot.bounds))
                assertFalse(layout.drop.overlaps(slot.bounds))
                assertFalse(layout.close.overlaps(slot.bounds))
                assertEquals(slot, layout.slotAt(slot.bounds.x + slot.bounds.width / 2, slot.bounds.y + slot.bounds.height / 2))
            }
        }
    }
}
