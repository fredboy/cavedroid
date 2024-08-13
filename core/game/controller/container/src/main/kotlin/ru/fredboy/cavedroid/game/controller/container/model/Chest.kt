package ru.fredboy.cavedroid.game.controller.container.model

import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class Chest(
    fallbackItem: Item,
    initialItems: List<InventoryItem>? = null,
) : Container(
    size = SIZE,
    fallbackItem = fallbackItem,
    initialItems = initialItems
) {

    override fun update(itemByKey: GetItemByKeyUseCase) {
        // no-op
    }

    companion object {
        private const val SIZE = 27
    }
}
