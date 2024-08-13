package ru.fredboy.cavedroid.game.controller.drop

import ru.fredboy.cavedroid.game.controller.drop.listener.DropAddedListener
import ru.fredboy.cavedroid.game.controller.drop.listener.DropRemovedListener
import ru.fredboy.cavedroid.game.controller.drop.model.Drop
import ru.fredboy.cavedroid.domain.items.model.item.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item

interface DropController {

    val size: Int

    fun getAllDrop(): Collection<Drop>

    fun addDrop(drop: Drop)

    fun addDrop(x: Float, y: Float, item: Item, count: Int)

    fun addDrop(x: Float, y: Float, inventoryItem: InventoryItem)

    fun forEach(action: (Drop) -> Unit)

    fun update(delta: Float)

    fun addDropAddedListener(listener: DropAddedListener)

    fun removeDropAddedListener(listener: DropAddedListener)

    fun addDropRemovedListener(listener: DropRemovedListener)

    fun removeDropRemovedListener(listener: DropRemovedListener)

    fun dispose()

}