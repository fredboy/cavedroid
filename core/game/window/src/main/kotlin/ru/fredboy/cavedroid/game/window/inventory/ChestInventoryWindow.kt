package ru.fredboy.cavedroid.game.window.inventory

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.game.window.GameWindowType

class ChestInventoryWindow(val chest: Chest, val otherChest: Chest? = null) : AbstractInventoryWindow() {

    val isLarge get() = otherChest != null
    val textureKey get() = if (isLarge) "chest_large" else "chest"
    val contentsRows get() = if (isLarge) 6 else 3
    val inventoryTop get() = if (isLarge) 140f else 86f

    // A live view: replacing a slot writes to its original half, including saved inventories.
    val items: MutableList<InventoryItem> = object : AbstractMutableList<InventoryItem>() {
        override val size get() = chest.size + (otherChest?.size ?: 0)
        private fun half(index: Int): MutableList<InventoryItem> {
            checkElementIndex(index)
            return if (index < chest.size) chest.items else requireNotNull(otherChest).items
        }
        private fun checkElementIndex(index: Int) {
            require(index in 0 until size)
        }
        override fun get(index: Int) = half(index)[index % chest.size]
        override fun set(index: Int, element: InventoryItem): InventoryItem {
            val items = half(index)
            val slot = index % chest.size
            return items.set(slot, element)
        }
        override fun add(index: Int, element: InventoryItem): Unit = error("Chest size is fixed")
        override fun removeAt(index: Int): InventoryItem = error("Chest size is fixed")
    }

    override val type = GameWindowType.CHEST

    override var selectedItem: InventoryItem? = null
}
