package ru.deadsoftware.cavedroid.game.model.craft

import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.model.item.Item

data class CraftingRecipe(
    val input: List<Regex>,
    val output: CraftingResult
)

data class CraftingResult(
    val item: Item,
    val amount: Int,
) {
    fun toInventoryItem() = InventoryItem(item, amount)
}
