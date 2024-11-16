package ru.fredboy.cavedroid.domain.items.model.inventory

import ru.fredboy.cavedroid.domain.items.model.item.Item

class Inventory @JvmOverloads constructor(
    val size: Int,
    private val fallbackItem: Item.None,
    initialItems: List<InventoryItem>? = null
) {

    private val _items: Array<InventoryItem> = Array(size) { index ->
        initialItems?.getOrNull(index) ?: fallbackItem.toInventoryItem()
    }

    val items get() = _items.asList() as MutableList<InventoryItem>

    fun getAvailableSlotForItem(item: Item): Int {
        for (i in _items.indices) {
            val inventoryItem = _items[i]

            if (item == inventoryItem.item && inventoryItem.canBeAdded()) {
                return i
            }
        }

        for (i in _items.indices) {
            val inventoryItem = _items[i]

            if (inventoryItem.item.isNone()) {
                return i
            }
        }

        return -1
    }

    fun canPickItem(item: Item): Boolean {
        return getAvailableSlotForItem(item) >= 0
    }

    fun addItem(item: Item) {
        _items.copyInto(
            destination = _items,
            destinationOffset = 1,
            startIndex = 0,
            endIndex = size - 1
        )

        _items[0] = item.toInventoryItem(item.params.maxStack)
    }

    /**
     * @return true if all amount was picked up
     */
    fun pickUpItem(pickingItem: InventoryItem): Boolean {
        val slot = getAvailableSlotForItem(pickingItem.item).takeIf { it >= 0 } ?: return false
        val inventoryItem = _items[slot]

        if (inventoryItem.item == pickingItem.item) {
            if (inventoryItem.canBeAdded(pickingItem.amount)) {
                inventoryItem.add(pickingItem.amount)
                return true
            } else {
                val addCount = inventoryItem.item.params.maxStack - inventoryItem.amount
                inventoryItem.add(addCount)
                pickingItem.subtract(addCount)
                return false
            }
        } else {
            _items[slot] = pickingItem
            return true
        }
    }

    @JvmOverloads
    fun decreaseItemAmount(slot: Int, count: Int = 1) {
        val item = _items[slot]
        item.subtract(count)
        if (item.amount <= 0) {
            _items[slot] = fallbackItem.toInventoryItem()
        }
    }

    fun clear() {
        for (i in _items.indices) {
            _items[i] = fallbackItem.toInventoryItem()
        }
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1
    }
}