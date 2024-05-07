package ru.deadsoftware.cavedroid.game.mobs.player

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.objects.Drop
import java.io.Serializable

class Inventory(
    val size: Int,
    val hotbarSize: Int,
    gameItemsHolder: GameItemsHolder
) : Serializable {

    init {
        if (size < 0 || hotbarSize < 0 || hotbarSize > size) {
            throw IllegalArgumentException("Invalid inventory sizes: hotbarSize=$hotbarSize; size=$size")
        }
    }

    private val _items = Array(size) { InventoryItem(gameItemsHolder.fallbackItem) }

    val items get() = _items.asList() as MutableList<InventoryItem>

    val hotbarItems get() = items.subList(0, hotbarSize)

    var activeSlot = 0
        set(value) {
            if (value in 0 ..< hotbarSize) {
                field = value
            }
        }

    val activeItem get() = items[activeSlot]

    fun initItems(gameItemsHolder: GameItemsHolder) {
        items.forEach { item ->
            item.init(gameItemsHolder)
        }
    }

    private fun getItemPickSlot(item: Item): Int {
        for (i in items.indices) {
            val inventoryItem = items[i]

            if (item == inventoryItem.item && inventoryItem.canBeAdded()) {
                return i
            }

            if (inventoryItem.item.isNone()) {
                return i
            }
        }

        return -1
    }

    fun canPickItem(item: Item): Boolean {
        return getItemPickSlot(item) >= 0
    }

    fun pickDrop(drop: Drop) {
        val slot = getItemPickSlot(drop.item).takeIf { it >= 0 } ?: return
        val inventoryItem = items[slot]

        if (inventoryItem.item == drop.item) {
            inventoryItem.add()
            drop.pickedUp = true
        } else {
            _items[slot] = drop.item.toInventoryItem()
            drop.pickedUp = true
        }
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
}