package ru.fredboy.cavedroid.entity.drop.abstraction

import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem

interface DropAdapter {

    fun dropInventory(x: Float, y: Float, inventory: Inventory)

    fun dropItems(x: Float, y: Float, items: List<InventoryItem>)
}
