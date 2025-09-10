package ru.fredboy.cavedroid.game.controller.drop

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.domain.assets.repository.DropSoundAssetsRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.drop.DropQueue
import ru.fredboy.cavedroid.entity.drop.abstraction.DropWorldAdapter
import ru.fredboy.cavedroid.entity.drop.model.Drop
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import java.util.*
import javax.inject.Inject

@GameScope
class DropController @Inject constructor(
    private val dropWorldAdapter: DropWorldAdapter,
    private val dropQueue: DropQueue,
    private val playerAdapter: PlayerAdapter,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val dropSoundAssetsRepository: DropSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
) : OnBlockDestroyedListener {

    private val drops = LinkedList<Drop>()

    constructor(
        dropWorldAdapter: DropWorldAdapter,
        dropQueue: DropQueue,
        initialDrop: Collection<Drop>,
        playerAdapter: PlayerAdapter,
        getItemByKeyUseCase: GetItemByKeyUseCase,
        dropSoundAssetsRepository: DropSoundAssetsRepository,
        soundPlayer: SoundPlayer,
    ) : this(dropWorldAdapter, dropQueue, playerAdapter, getItemByKeyUseCase, dropSoundAssetsRepository, soundPlayer) {
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
        if (dropQueue.queue.isNotEmpty()) {
            val queued = dropQueue.queue.poll()
            addDrop(queued.x, queued.y, queued.item, getRandomInitialForce())
        }
    }

    private fun playPickUpSound(drop: Drop) {
        val sound = dropSoundAssetsRepository.getDropPopSound() ?: return
        soundPlayer.playSoundAtPosition(
            sound = sound,
            soundX = drop.position.x,
            soundY = drop.position.y,
            playerX = playerAdapter.x,
            playerY = playerAdapter.y,
        )
    }

    @Suppress("unused")
    fun update(delta: Float) {
        drainDropQueue()

        val iterator = drops.iterator()

        while (iterator.hasNext()) {
            val drop = iterator.next()
            if (drop.isPickedUp || TimeUtils.timeSinceMillis(drop.timestamp) > DROP_TTL_ML) {
                if (drop.isPickedUp) {
                    playPickUpSound(drop)
                }
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

    override fun onBlockDestroyed(
        block: Block,
        x: Int,
        y: Int,
        layer: Layer,
        withDrop: Boolean,
        destroyedByPlayer: Boolean,
    ) {
        if (!withDrop) {
            return
        }

        val toolRequirementMet = destroyedByPlayer.ifTrue {
            playerAdapter.activeItem.item.let { itemInHand ->
                val toolLevel = (itemInHand as? Item.Tool)?.level?.takeIf {
                    block.params.toolType == itemInHand.javaClass
                } ?: 0

                toolLevel >= block.params.toolLevel
            }
        } ?: (block.params.toolLevel == 0)

        block.getDropItem(getItemByKeyUseCase, toolRequirementMet)?.let { dropItem ->
            addDrop(
                x = x + .5f,
                y = y + .5f,
                item = dropItem.item,
                count = dropItem.amount,
                initialForce = getRandomInitialForce(),
            )
        }
    }

    companion object {
        private const val DROP_TTL_ML = 600_000
    }
}
