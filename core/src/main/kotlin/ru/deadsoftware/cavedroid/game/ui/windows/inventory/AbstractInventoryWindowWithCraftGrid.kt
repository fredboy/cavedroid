package ru.deadsoftware.cavedroid.game.ui.windows.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

abstract class AbstractInventoryWindowWithCraftGrid(
    itemsRepository: ItemsRepository,
) : AbstractInventoryWindow() {

    private val _items = Array(10) { itemsRepository.fallbackItem.toInventoryItem() }

    val items get() = _items.asList()

    val craftingItems get() = items.subList(0, 9) as MutableList<InventoryItem>

    val craftResultList get() = items.subList(9, 10) as MutableList<InventoryItem>

    var craftResult: InventoryItem
        get() = craftResultList[0]
        set(value) {
            craftResultList[0] = value
        }

}