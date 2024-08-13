package ru.fredboy.cavedroid.game.controller.mob.model

import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item

class Inventory @JvmOverloads constructor(
    val size: Int,
    val hotbarSize: Int,
    private val fallbackItem: Item.None,
    initialItems: List<InventoryItem>? = null
) {

    private val _items: Array<InventoryItem>

    init {
        if (size < 0 || hotbarSize < 0 || hotbarSize > size) {
            throw IllegalArgumentException("Invalid inventory sizes: hotbarSize=$hotbarSize; size=$size")
        }

        _items = Array(size) { index -> initialItems?.getOrNull(index) ?: fallbackItem.toInventoryItem() }
    }

    val items get() = _items.asList() as MutableList<InventoryItem>

    val hotbarItems get() = items.subList(0, hotbarSize)

    private var _activeSlot = 0

    var activeSlot
        get() = _activeSlot
        set(value) {
            if (value in 0 ..< hotbarSize) {
                _activeSlot = value
            }
        }

    val activeItem get() = _items[activeSlot]

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

    @JvmOverloads
    fun decreaseItemAmount(slot: Int, count: Int = 1) {
        val item = _items[slot]
        item.subtract(count)
        if (item.amount <= 0) {
            _items[slot] = fallbackItem.toInventoryItem()
        }
    }

    @JvmOverloads
    fun decreaseCurrentItemAmount(count: Int = 1) {
        decreaseItemAmount(activeSlot, count)
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