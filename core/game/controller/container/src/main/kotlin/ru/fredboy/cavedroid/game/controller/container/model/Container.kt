package ru.fredboy.cavedroid.game.controller.container.model

import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

abstract class Container(
    val size: Int,
    protected val fallbackItem: Item,
    initialItems: List<InventoryItem>? = null
) {

    private val _items = Array(size) { index ->
        initialItems?.getOrNull(index) ?: fallbackItem.toInventoryItem()
    }

    val items get() = _items.asList() as MutableList<InventoryItem>

    abstract fun update(itemByKey: GetItemByKeyUseCase)

}