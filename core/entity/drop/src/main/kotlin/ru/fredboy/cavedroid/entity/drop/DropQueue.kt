package ru.fredboy.cavedroid.entity.drop

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory
import ru.fredboy.cavedroid.entity.drop.model.QueuedDrop
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

@GameScope
class DropQueue @Inject constructor() {

    val queue: Queue<QueuedDrop> = LinkedList()

    fun offerInventory(x: Float, y: Float, inventory: Inventory) {
        inventory.items.forEach { item ->
            queue.offer(QueuedDrop(x, y, item))
        }
    }
}
