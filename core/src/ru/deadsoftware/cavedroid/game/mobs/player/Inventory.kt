package ru.deadsoftware.cavedroid.game.mobs.player

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.objects.drop.Drop
import ru.deadsoftware.cavedroid.game.ui.TooltipManager
import java.io.Serializable

class Inventory(
    val size: Int,
    val hotbarSize: Int,
    gameItemsHolder: GameItemsHolder,
    tooltipManager: TooltipManager,
) : Serializable {

    @Suppress("UNNECESSARY_LATEINIT")
    @Transient
    private lateinit var tooltipManager: TooltipManager

    @Suppress("UNNECESSARY_LATEINIT")
    @Transient
    private lateinit var fallbackItem: InventoryItem

    init {
        fallbackItem = gameItemsHolder.fallbackItem.toInventoryItem()
        this.tooltipManager = tooltipManager

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
                showCurrentItemTooltip()
            }
        }

    fun showCurrentItemTooltip() {
        tooltipManager.showHotbarTooltip(activeItem.item.params.name)
    }

    val activeItem get() = _items[activeSlot]

    fun initItems(gameItemsHolder: GameItemsHolder, tooltipManager: TooltipManager) {
        this.tooltipManager = tooltipManager
        fallbackItem = gameItemsHolder.fallbackItem.toInventoryItem()
        _items.forEach { item ->
            item.init(gameItemsHolder)
        }
    }

    private fun getItemPickSlot(drop: Drop): Int {
        val item = drop.item

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

    fun canPickItem(drop: Drop): Boolean {
        return getItemPickSlot(drop) >= 0
    }

    fun pickDrop(drop: Drop) {
        val slot = getItemPickSlot(drop).takeIf { it >= 0 } ?: return
        val inventoryItem = _items[slot]

        if (inventoryItem.item == drop.item) {
            if (inventoryItem.canBeAdded(drop.amount)) {
                inventoryItem.add(drop.amount)
                drop.pickedUp = true
            } else {
                val addCount = inventoryItem.item.params.maxStack - inventoryItem.amount
                inventoryItem.add(addCount)
                drop.subtract(addCount)
                pickDrop(drop)
            }
        } else {
            _items[slot] = drop.item.toInventoryItem(drop.amount)
            if (slot == activeSlot) {
                showCurrentItemTooltip()
            }
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
        showCurrentItemTooltip()
    }

    @JvmOverloads
    fun decreaseItemAmount(slot: Int, count: Int = 1) {
        val item = _items[slot]
        item.subtract(count)
        if (item.amount <= 0) {
            _items[slot] = fallbackItem
        }
    }

    @JvmOverloads
    fun decreaseCurrentItemAmount(count: Int = 1) {
        decreaseItemAmount(activeSlot, count)
    }

    fun clear() {
        for (i in _items.indices) {
            _items[i] = fallbackItem
        }
    }
}