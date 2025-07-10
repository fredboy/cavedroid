package ru.fredboy.cavedroid.game.controller.drop

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.drop.model.Drop
import java.util.*
import javax.inject.Inject

@GameScope
class DropController @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val dropWorldAdapter: DropWorldAdapter,
    private val dropQueue: DropQueue,
) : OnBlockDestroyedListener {

    private val drops = LinkedList<Drop>()

    constructor(
        itemsRepository: ItemsRepository,
        dropWorldAdapter: DropWorldAdapter,
        dropQueue: DropQueue,
        initialDrop: Collection<Drop>,
    ) : this(itemsRepository, dropWorldAdapter, dropQueue) {
        drops.addAll(initialDrop.filterNot { drop -> drop.item.isNone() })
    }

    val size get() = drops.size

    init {
        dropWorldAdapter.addOnBlockDestroyedListener(this)
    }

    fun getAllDrop(): Collection<Drop> {
        return drops
    }

    private fun addDrop(drop: Drop, x: Float, y: Float, initialForce: Vector2? = null) {
        if (drop.item.isNone()) {
            return
        }

        drop.spawn(x, y, dropWorldAdapter.getBox2dWorld())

        if (initialForce != null) {
            drop.body.applyForceToCenter(initialForce, true)
        }

        drops.add(drop)
    }

    fun addDrop(x: Float, y: Float, item: Item, count: Int, initialForce: Vector2? = null) {
        addDrop(Drop(item, count), x, y, initialForce)
    }

    fun addDrop(x: Float, y: Float, inventoryItem: InventoryItem, initialForce: Vector2? = null) {
        addDrop(x, y, inventoryItem.item, inventoryItem.amount, initialForce)
    }

    fun forEach(action: (Drop) -> Unit) {
        drops.forEach(action)
    }

    private fun getRandomInitialForce(): Vector2 {
        return Vector2(MathUtils.random(-20f, 20f), -20f)
    }

    private fun drainDropQueue() {
        while (dropQueue.queue.isNotEmpty()) {
            val queued = dropQueue.queue.poll()
            addDrop(queued.x, queued.y, queued.item, getRandomInitialForce())
        }
    }

    @Suppress("unused")
    fun update(delta: Float) {
        drainDropQueue()

        val iterator = drops.iterator()

        while (iterator.hasNext()) {
            val drop = iterator.next()
            if (drop.isPickedUp) {
                drop.dispose()
                iterator.remove()
            } else {
                drop.update(dropWorldAdapter, delta)
            }
        }
    }

    fun dispose() {
        drops.forEach { it.dispose() }
        drops.clear()
    }

    override fun onBlockDestroyed(block: Block, x: Int, y: Int, layer: Layer, withDrop: Boolean) {
        if (!withDrop) {
            return
        }

        val dropInfo = block.params.dropInfo ?: return
        val item = itemsRepository.getItemByKey(dropInfo.itemKey).takeIf { !it.isNone() } ?: return

        addDrop(
            x = x + .5f,
            y = y + .5f,
            item = item,
            count = dropInfo.count,
            initialForce = getRandomInitialForce(),
        )
    }
}
