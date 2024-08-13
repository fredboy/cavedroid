package ru.fredboy.cavedroid.game.controller.drop.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.drop.listener.DropAddedListener
import ru.fredboy.cavedroid.game.controller.drop.listener.DropRemovedListener
import ru.fredboy.cavedroid.game.controller.drop.model.Drop
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import java.util.LinkedList
import javax.inject.Inject

@GameScope
class DropControllerImpl @Inject constructor() : DropController {

    private val drops = LinkedList<Drop>()

    private val dropAddedListeners = HashSet<DropAddedListener>()
    private val dropRemovedListeners = HashSet<DropRemovedListener>()

    constructor(initialDrop: Collection<Drop>) : this() {
        drops.addAll(initialDrop)
    }

    override val size get() = drops.size

    override fun getAllDrop(): Collection<Drop> {
        return drops
    }

    override fun addDrop(drop: Drop) {
        drops.add(drop)
        dropAddedListeners.forEach { listener ->
            listener.onDropAdded(drop)
        }
    }

    override fun addDrop(x: Float, y: Float, item: Item, count: Int) {
        addDrop(Drop(x, y, item, count))
    }

    override fun addDrop(x: Float, y: Float, inventoryItem: InventoryItem) {
        addDrop(x, y, inventoryItem.item, inventoryItem.amount)
    }

    override fun forEach(action: (Drop) -> Unit) {
        drops.forEach(action)
    }

    override fun update(delta: Float) {
        val iterator = drops.iterator()

        while (iterator.hasNext()) {
            val drop = iterator.next();
            if (drop.isPickedUp) {
                iterator.remove()
                dropRemovedListeners.forEach { listener ->
                    listener.onDropRemoved(drop)
                }
            }
        }
    }

    override fun addDropAddedListener(listener: DropAddedListener) {
        dropAddedListeners.add(listener)
    }

    override fun removeDropAddedListener(listener: DropAddedListener) {
        dropAddedListeners.remove(listener)
    }

    override fun addDropRemovedListener(listener: DropRemovedListener) {
        dropRemovedListeners.add(listener)
    }

    override fun removeDropRemovedListener(listener: DropRemovedListener) {
        dropRemovedListeners.remove(listener)
    }

    override fun dispose() {
        dropAddedListeners.clear()
        dropAddedListeners.clear()
        drops.clear()
    }

}