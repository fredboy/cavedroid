package ru.fredboy.cavedroid.game.controller.drop.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import ru.fredboy.cavedroid.game.controller.drop.DropController
import javax.inject.Inject

@GameScope
internal class DropAdapterImpl @Inject constructor(
    private val dropController: DropController,
) : DropAdapter {

    override fun dropInventory(x: Float, y: Float, inventory: Inventory) {
        dropItems(x, y, inventory.items)
        inventory.clear()
    }

    override fun dropItems(x: Float, y: Float, items: List<InventoryItem>) {
        items.forEach { item -> dropController.addDrop(x, y, item) }
    }
}
