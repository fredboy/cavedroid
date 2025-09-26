package ru.fredboy.cavedroid.game.controller.container

import box2dLight.Light
import box2dLight.PointLight
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.physics.box2d.Filter
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerFactory
import ru.fredboy.cavedroid.entity.container.abstraction.ContainerWorldAdapter
import ru.fredboy.cavedroid.entity.container.model.Container
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.entity.drop.abstraction.DropAdapter
import javax.inject.Inject

@GameScope
class ContainerController @Inject constructor(
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    private val containerWorldAdapter: ContainerWorldAdapter,
    private val containerFactory: ContainerFactory,
    private val dropAdapter: DropAdapter,
) : OnBlockPlacedListener,
    OnBlockDestroyedListener {

    val containerMap = mutableMapOf<ContainerCoordinates, Container>()

    val size get() = containerMap.size

    init {
        containerWorldAdapter.addOnBlockPlacedListener(this)
        containerWorldAdapter.addOnBlockDestroyedListener(this)
    }

    private fun getContainerKey(x: Int, y: Int, z: Int): ContainerCoordinates {
        return ContainerCoordinates(x, y, z)
    }

    fun getContainer(x: Int, y: Int, z: Int): Container? {
        return containerMap[getContainerKey(x, y, z)]
    }

    fun addContainer(x: Int, y: Int, z: Int, container: Container) {
        val key = getContainerKey(x, y, z)
        if (containerMap.containsKey(key)) {
            resetContainer(x, y, z)
        }

        containerMap[key] = container
    }

    private fun retrieveContainer(x: Int, y: Int, z: Int): Container? {
        return containerMap.remove(getContainerKey(x, y, z))
    }

    fun resetContainer(x: Int, y: Int, z: Int) {
        retrieveContainer(x, y, z)
    }

    fun destroyContainer(x: Int, y: Int, z: Int) {
        retrieveContainer(x, y, z)?.let { container ->
            dropAdapter.dropInventory(x.toFloat(), y.toFloat(), container.inventory)

            if (container is Furnace) {
                container.lightSource?.remove(true)
            }
        }
    }

    @Suppress("unused")
    fun update(delta: Float) {
        val iterator = containerMap.iterator()
        while (iterator.hasNext()) {
            val (coordinates, container) = iterator.next()

            if (!containerWorldAdapter.checkContainerAtCoordinates(coordinates, container.type)) {
                logger.w { "Removing orphaned ${container::class.simpleName} at $coordinates" }
                iterator.remove()
            } else {
                container.update(getItemByKeyUseCase)

                if (container is Furnace && container.lightSource == null) {
                    container.lightSource = createFurnaceLightSource(coordinates.x, coordinates.y, container)
                }
            }
        }
    }

    fun dispose() {
        containerMap.clear()
        containerWorldAdapter.removeOnBlockPlacedListener(this)
        containerWorldAdapter.removeOnBlockDestroyedListener(this)
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
    }

    private fun createFurnaceLightSource(x: Int, y: Int, furnace: Furnace): Light {
        return PointLight(
            containerWorldAdapter.getRayHandler(),
            128,
            Color().apply { a = 1f },
            13f,
            x + 0.5f,
            y + 0.5f,
        ).apply {
            val filter = Filter().apply {
                maskBits = PhysicsConstants.CATEGORY_OPAQUE
            }
            setContactFilter(filter)
            setSoftnessLength(3f)
            isActive = furnace.isActive
        }
    }

    companion object {
        private const val TAG = "ContainerControllerImpl"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
