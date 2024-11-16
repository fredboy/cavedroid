package ru.fredboy.cavedroid.entity.container.model

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class Chest(
    fallbackItem: Item.None,
    initialItems: List<InventoryItem>? = null,
) : Container(
    size = SIZE,
    fallbackItem = fallbackItem,
    initialItems = initialItems
) {

    override val type get() = Block.Chest::class

    override fun update(itemByKey: GetItemByKeyUseCase) {
        // no-op
    }

    companion object {
        private const val SIZE = 27
    }
}
