package ru.fredboy.cavedroid.game.controller.container

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.removeFirst
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.abstraction.MobSpawnerAdapter
import ru.fredboy.cavedroid.entity.container.model.Chest
import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.container.model.Spawner
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import java.lang.ref.WeakReference
import java.util.LinkedList
import javax.inject.Inject

@GameScope
class ContainerController @Inject constructor(
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val containerWorldAdapter: ContainerWorldAdapter,
    private val containerFactory: ContainerFactory,
    private val dropAdapter: DropAdapter,
    private val mobSpawnerAdapter: MobSpawnerAdapter,
) : OnBlockPlacedListener,
    OnBlockDestroyedListener {

    private val furnaceStateChangedListeners = LinkedList<WeakReference<FurnaceStateChangedListener>>()

    val containerMap = mutableMapOf<ContainerCoordinates, Container>()

    val size get() = containerMap.size

    init {
        containerWorldAdapter.addOnBlockPlacedListener(this)
        containerWorldAdapter.addOnBlockDestroyedListener(this)
    }

    private fun getContainerKey(x: Int, y: Int, z: Int): ContainerCoordinates {
        return ContainerCoordinates(x, y, z)
    }

    fun addFurnaceListener(listener: FurnaceStateChangedListener) {
        furnaceStateChangedListeners.add(WeakReference(listener))
    }

    fun removeFurnaceListener(listener: FurnaceStateChangedListener) {
        furnaceStateChangedListeners.removeFirst { it.get() == listener }
    }

    private fun notifyFurnaceStateUpdated(x: Int, y: Int, z: Int, isActive: Boolean) {
        furnaceStateChangedListeners.removeAll { listener ->
            listener.get()?.let {
                it.onFurnaceStateChanged(x, y, z, isActive)
                return@removeAll false
            }

            logger.w { "An empty FurnaceStateChangedListener weak reference was removed!" }
            true
        }
    }

    fun getContainer(x: Int, y: Int, z: Int): Container? {
        return containerMap[getContainerKey(containerWorldAdapter.containerX(x), y, z)]
    }

    /** Left half always precedes the right half, regardless of which block was opened. */
    fun getChestPair(x: Int, y: Int, z: Int): Pair<Chest, Chest?>? {
        val chest = getContainer(x, y, z) as? Chest ?: return null
        reconcileChest(x, y, z)
        val key = containerWorldAdapter.chestBlockKey(x, y, z)
        val offset = when (key) {
            "chest_large_left" -> 1
            "chest_large_right" -> -1
            else -> return chest to null
        }
        val expected = if (offset == 1) "chest_large_right" else "chest_large_left"
        val other = getContainer(x + offset, y, z) as? Chest
        if (other == null || containerWorldAdapter.chestBlockKey(x + offset, y, z) != expected) return chest to null
        return if (offset == 1) chest to other else other to chest
    }

    private fun reconcileChest(x: Int, y: Int, z: Int) {
        val key = containerWorldAdapter.chestBlockKey(x, y, z) ?: return
        if (key == "chest_large_left" || key == "chest_large_right") {
            val offset = if (key == "chest_large_left") 1 else -1
            val expected = if (offset == 1) "chest_large_right" else "chest_large_left"
            val neighbor = ContainerCoordinates(containerWorldAdapter.containerX(x + offset), y, z)
            if (containerWorldAdapter.isContainerChunkLoaded(neighbor) && containerWorldAdapter.chestBlockKey(neighbor.x, y, z) != expected) {
                containerWorldAdapter.setChestBlockKey(x, y, z, "chest")
            }
            return
        }
        if (key != "chest") return
        for (offset in listOf(-1, 1)) {
            val nx = containerWorldAdapter.containerX(x + offset)
            if (nx == containerWorldAdapter.containerX(x) || getContainer(nx, y, z) !is Chest) continue
            if (containerWorldAdapter.chestBlockKey(nx, y, z) != "chest") continue
            containerWorldAdapter.setChestBlockKey(x, y, z, if (offset == 1) "chest_large_left" else "chest_large_right")
            containerWorldAdapter.setChestBlockKey(nx, y, z, if (offset == 1) "chest_large_right" else "chest_large_left")
            break
        }
    }

    fun onContainerAdded(x: Int, y: Int, z: Int, container: Container) {
        if (container is Chest) reconcileChest(x, y, z)
        if (container is Furnace) {
            container.notifyStateChanged = { isActive ->
                notifyFurnaceStateUpdated(x, y, z, isActive)
            }
        }
    }

    fun addContainer(x: Int, y: Int, z: Int, container: Container) {
        val key = getContainerKey(x, y, z)
        if (containerMap.containsKey(key)) {
            resetContainer(x, y, z)
        }

        containerMap[key] = container
        onContainerAdded(x, y, z, container)
    }

    private fun retrieveContainer(x: Int, y: Int, z: Int): Container? {
        return containerMap.remove(getContainerKey(x, y, z))
    }

    fun resetContainer(x: Int, y: Int, z: Int) {
        retrieveContainer(x, y, z)
    }

    /**
     * Removes a container from memory when its chunk unloads, disposing any held resources (e.g. a
     * furnace light). Unlike [destroyContainer] this keeps the contents — they have been persisted to
     * the chunk and will be restored when the chunk loads again — so the inventory is not dropped.
     */
    fun evictContainer(coordinates: ContainerCoordinates) {
        val container = containerMap.remove(coordinates) ?: return
        if (container is Disposable) {
            container.dispose()
        }
    }

    fun destroyContainer(x: Int, y: Int, z: Int) {
        retrieveContainer(x, y, z)?.let { container ->
            dropAdapter.dropInventory(x.toFloat(), y.toFloat(), container.inventory)

            if (container is Disposable) {
                container.dispose()
            }
        }
    }

    fun update(delta: Float) {
        val iterator = containerMap.iterator()
        while (iterator.hasNext()) {
            val (coordinates, container) = iterator.next()

            if (!containerWorldAdapter.isContainerChunkLoaded(coordinates)) {
                continue
            }

            if (!containerWorldAdapter.checkContainerAtCoordinates(coordinates, container.type)) {
                logger.w { "Removing orphaned ${container::class.simpleName} at $coordinates" }
                iterator.remove()
            } else {
                if (container is Chest) reconcileChest(coordinates.x, coordinates.y, coordinates.z)
                container.update(getItemByKeyUseCase)
                if (container is Spawner) {
                    container.update(delta, coordinates, mobSpawnerAdapter)
                }

                if (container is Furnace && container.lightSource == null) {
                    container.lightSource = createFurnaceLightSource(coordinates.x, coordinates.y, container)
                }
            }
        }
    }

    fun dispose() {
        containerMap.values.asSequence()
            .filterIsInstance<Disposable>()
            .forEach { container -> container.dispose() }

        containerMap.clear()
        containerWorldAdapter.removeOnBlockPlacedListener(this)
        containerWorldAdapter.removeOnBlockDestroyedListener(this)
        furnaceStateChangedListeners.clear()
    }

    override fun onBlockDestroyed(
        block: Block,
        x: Int,
        y: Int,
        layer: Layer,
        withDrop: Boolean,
        destroyedByPlayer: Boolean,
    ) {
        destroyContainer(x, y, layer.z)
    }

    override fun onBlockPlaced(block: Block, x: Int, y: Int, layer: Layer) {
        if (block is Block.Container) {
            val container = containerFactory.createContainer(block)
            addContainer(x, y, layer.z, container)
        }
        for (nx in listOf(x - 1, x + 1)) {
            if (getContainer(nx, y, layer.z) is Chest) reconcileChest(nx, y, layer.z)
        }
    }

    private fun createFurnaceLightSource(x: Int, y: Int, furnace: Furnace): LightHandle {
        return containerWorldAdapter.createFurnaceLight(x + 0.5f, y + 0.5f).apply {
            isActive = furnace.isActive
        }
    }

    companion object {
        private const val TAG = "ContainerControllerImpl"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
