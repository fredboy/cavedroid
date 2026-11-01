package ru.fredboy.cavedroid.game.window

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.craft.CraftingEntry
import ru.fredboy.cavedroid.domain.items.model.craft.CraftingRecipe
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.inventory.CraftingInventoryWindow

class TouchRecipePagingTest {
    @Test
    fun `compact recipe pages retain every recipe and use the selected mobile page for preview and autofill`() {
        val none = Item.None(mockk { every { maxStack } returns 64 })
        fun item(itemKey: String) = Item.Normal(
            mockk {
                every { key } returns itemKey
                every { maxStack } returns 64
            },
            mockk(),
        )
        val ingredients = (0..52).map { item("ingredient_$it") }
        val results = (0..52).map { item("result_$it") }
        val entries = results.mapIndexed { index, result ->
            CraftingEntry(listOf(CraftingRecipe(listOf(Regex("ingredient_$index")) + List(8) { null }, false, 1, false)), result)
        }
        val items = mockk<ItemsRepository> {
            every { fallbackItem } returns none
            every { getAllItems() } returns ingredients + results
            every { getAllCraftingRecipes() } returns entries
        }
        val window = CraftingInventoryWindow(items)
        val pages = (0..2).flatMap { page ->
            window.recipeBookPage = page
            window.getVisibleCraftingRecipes(items, TouchWindowLayout.RECIPES_PER_PAGE).map { it.second }
        }
        assertEquals(results, pages)
        window.recipeBookPage = 0
        assertEquals(25, window.getVisibleCraftingRecipes(items).size)
        window.recipeBookPage = 1
        window.selectedRecipe = 3
        assertSame(ingredients[27], window.getPhantomRecipe(items, TouchWindowLayout.RECIPES_PER_PAGE)[0].item)
        val inventory = Inventory(36, none).apply { this.items[0] = ingredients[27].toInventoryItem(4) }
        window.tryFillPhantomRecipe(mockk(relaxed = true), mockk(relaxed = true), inventory, items, TouchWindowLayout.RECIPES_PER_PAGE)
        assertSame(ingredients[27], window.craftingItems[0].item)
        assertEquals(1, window.craftingItems[0].amount)
        assertEquals(3, inventory.items[0].amount)
    }
}
