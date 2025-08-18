package ru.fredboy.cavedroid.entity.drop

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.entity.drop.model.QueuedDrop
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

@GameScope
class DropQueue @Inject constructor() {

    val queue: Queue<QueuedDrop> = LinkedList()

    fun offerInventory(x: Float, y: Float, inventory: Inventory) {
        offerItems(x, y, inventory.items)
    }

    fun offerItems(x: Float, y: Float, items: List<InventoryItem>) {
        items.forEach { item ->
            offerItem(x, y, item)
        }
    }

    fun offerItem(x: Float, y: Float, item: InventoryItem) {
        queue.offer(QueuedDrop(x, y, item))
    }
}
