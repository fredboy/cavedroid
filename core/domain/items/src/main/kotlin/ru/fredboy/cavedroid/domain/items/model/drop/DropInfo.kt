package ru.fredboy.cavedroid.domain.items.model.drop

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

data class DropInfo(
    val itemKey: String,
    val requiresTool: Boolean,
    val amount: DropAmount,
) {

    fun toInventoryItem(itemByKey: GetItemByKeyUseCase): InventoryItem {
        val item = itemByKey[itemKey]
        val value = when (amount) {
            is DropAmount.ExactAmount -> amount.amount
            is DropAmount.RandomChance -> amount.amount
            is DropAmount.RandomRange -> amount.range.random()
        }

        val (amount, durability) = if (item is Item.Durable) {
            1 to value
        } else {
            value to 1
        }

        return item.toInventoryItem(amount, durability)
    }
}
