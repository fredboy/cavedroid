package ru.fredboy.cavedroid.game.controller.drop

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.blockCenterPx
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.drop.model.Drop
import java.util.*
import javax.inject.Inject

@GameScope
class DropController @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val dropWorldAdapter: DropWorldAdapter,
) : OnBlockDestroyedListener {

    private val drops = LinkedList<Drop>()

    constructor(
        itemsRepository: ItemsRepository,
        dropWorldAdapter: DropWorldAdapter,
        initialDrop: Collection<Drop>
    ) : this(itemsRepository, dropWorldAdapter) {
        drops.addAll(initialDrop.filterNot { drop -> drop.item.isNone() })
    }

    val size get() = drops.size

    init {
        dropWorldAdapter.addOnBlockDestroyedListener(this)
    }

    fun getAllDrop(): Collection<Drop> {
        return drops
    }

    fun addDrop(drop: Drop) {
        if (drop.item.isNone()) {
            return
        }

        drops.add(drop)
    }

    fun addDrop(x: Float, y: Float, item: Item, count: Int) {
        addDrop(Drop(x, y, item, count))
    }

    fun addDrop(x: Float, y: Float, inventoryItem: InventoryItem) {
        addDrop(x, y, inventoryItem.item, inventoryItem.amount)
    }

    fun forEach(action: (Drop) -> Unit) {
        drops.forEach(action)
    }

    fun update(delta: Float) {
        val iterator = drops.iterator()

        while (iterator.hasNext()) {
            val drop = iterator.next();
            if (drop.isPickedUp) {
                iterator.remove()
            }
        }
    }

    fun dispose() {
        drops.clear()
    }

    override fun onBlockDestroyed(block: Block, x: Int, y: Int, layer: Layer, withDrop: Boolean) {
        if (!withDrop) {
            return
        }

        val dropInfo = block.params.dropInfo ?: return
        val item = itemsRepository.getItemByKey(dropInfo.itemKey).takeIf { !it.isNone() } ?: return

        addDrop(
            x = x.blockCenterPx() - Drop.DROP_SIZE / 2,
            y = y.blockCenterPx() - Drop.DROP_SIZE / 2,
            item = item,
            count = dropInfo.count
        )
    }

}