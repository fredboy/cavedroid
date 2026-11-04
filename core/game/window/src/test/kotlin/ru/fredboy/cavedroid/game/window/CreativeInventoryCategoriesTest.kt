package ru.fredboy.cavedroid.game.window

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.item.CreativeCategory
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow.Tab

class CreativeInventoryCategoriesTest {
    @Test
    fun `categories use explicit membership regardless of item behavior and exclude the empty item`() {
        val none = Item.None(mockk())
        CreativeCategory.entries.forEach { category ->
            val normal = Item.Normal(mockk { every { creativeCategory } returns category }, mockk())
            val block = mockk<Item.Block> { every { params.creativeCategory } returns category }
            listOf(normal, block).forEach { item ->
                assertEquals(listOf(category), Tab.entries.filter { !it.isInventory && it.isRelevantItem(item) }.map { it.category })
            }
        }
        Tab.entries.forEach { assertFalse(it.isRelevantItem(none)) }
        assertEquals(7, Tab.entries.count { !it.isInventory })
        assertEquals(Tab.Inventory, Tab.entries.last())
    }

    @Test
    fun `each category sorts its catalog alphabetically by key regardless of display name`() {
        val items = mockk<ItemsRepository>()
        val window = CreativeInventoryTabsWindow(items)
        val catalog = CreativeCategory.entries.flatMap { category ->
            listOf("zebra", "apple", "middle").mapIndexed { index, suffix ->
                Item.Normal(
                    mockk {
                        every { creativeCategory } returns category
                        every { key } returns "${category.key}_$suffix"
                        every { name } returns "Display $index"
                    },
                    mockk(),
                )
            }
        }
        every { items.getAllItems() } returns catalog + Item.None(mockk())
        Tab.entries.filter { !it.isInventory }.forEach { tab ->
            window.selectedTab = tab
            assertEquals(
                listOf("apple", "middle", "zebra").map { "${tab.category!!.key}_$it" },
                window.getCatalogItems().map { it.params.key },
            )
        }
    }

    @Test
    fun `desktop scroll stops at the last full page while retaining a partial final row`() {
        val item = Item.Normal(mockk { every { creativeCategory } returns CreativeCategory.Building }, mockk())
        val items = mockk<ItemsRepository>()
        val window = CreativeInventoryTabsWindow(items)
        for ((count, maxScroll) in listOf(0 to 0, 9 to 0, 45 to 0, 46 to 1, 54 to 1, 55 to 2, 109 to 8)) {
            every { items.getAllItems() } returns List(count) { item }
            assertEquals(maxScroll, window.getMaxScroll(), "Item count: $count")
        }
    }

    @Test
    fun `desktop tabs fit the panel and each center and inner edge hits only its own tab`() {
        val tabs = Tab.entries.map { GameWindowsConfigs.CreativeTabs.tabBounds(it.ordinal, 194f, 138f, 28f) }
        assertEquals(0f, tabs.first().x)
        assertEquals(194f, tabs.last().x + tabs.last().width)
        tabs.forEachIndexed { index, rect ->
            for (x in listOf(rect.x + .1f, rect.x + rect.width / 2, rect.x + rect.width - .1f)) {
                assertEquals(listOf(index), tabs.indices.filter { tabs[it].contains(x, rect.y + rect.height / 2) })
            }
        }
    }

    @Test
    fun `touch has two complete rows of four tabs clear of inventory slots`() {
        val items = mockk<ItemsRepository>()
        Tab.entries.forEach { tab ->
            val layout = TouchWindowLayout(640f, 320f, CreativeInventoryTabsWindow(items).apply { selectedTab = tab })
            assertEquals(8, layout.tabs.size)
            assertEquals(listOf(4, 4), layout.tabs.groupBy { it.y }.values.map { it.size })
            layout.tabs.forEach { rect ->
                assertTrue(layout.bounds.contains(rect))
                assertTrue(rect.width >= TouchWindowLayout.MIN_BUTTON_SIZE && rect.height >= TouchWindowLayout.MIN_BUTTON_SIZE)
                layout.slots.forEach { assertFalse(rect.overlaps(it.bounds)) }
                layout.tabs.filter { it !== rect }.forEach { assertFalse(rect.overlaps(it)) }
            }
        }
    }
}
