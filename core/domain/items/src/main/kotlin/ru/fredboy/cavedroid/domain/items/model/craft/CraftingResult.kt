package ru.fredboy.cavedroid.domain.items.model.craft

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item

data class CraftingResult(
    val item: Item,
    val amount: Int,
) {
    fun toInventoryItem() = InventoryItem(item, amount)
}
