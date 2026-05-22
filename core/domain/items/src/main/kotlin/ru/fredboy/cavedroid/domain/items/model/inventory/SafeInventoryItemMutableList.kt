package ru.fredboy.cavedroid.domain.items.model.inventory

import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository

class SafeInventoryItemMutableList(
    private val itemsRepository: ItemsRepository,
    private val delegate: MutableList<InventoryItem>,
) : MutableList<InventoryItem> by delegate {

    override fun get(index: Int): InventoryItem {
        return delegate.getOrNull(index) ?: itemsRepository.fallbackItem.toInventoryItem()
    }

    override fun set(index: Int, element: InventoryItem): InventoryItem {
        if (index in delegate.indices) {
            return delegate.set(index, element)
        }

        return itemsRepository.fallbackItem.toInventoryItem()
    }
}

fun MutableList<InventoryItem>.asSafeInventoryList(itemsRepository: ItemsRepository): SafeInventoryItemMutableList {
    return SafeInventoryItemMutableList(itemsRepository, this)
}
