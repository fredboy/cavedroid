package ru.fredboy.cavedroid.game.controller.container.impl

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.container.listener.ContainerAddedListener
import ru.fredboy.cavedroid.game.controller.container.listener.ContainerRemovedListener
import ru.fredboy.cavedroid.game.controller.container.model.Container
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import javax.inject.Inject

@GameScope
class ContainerControllerImpl @Inject constructor(
    private val itemByKey: GetItemByKeyUseCase,
) : ContainerController {

    val containerMap = mutableMapOf<String, Container>()

    private val containerAddedListeners = mutableSetOf<ContainerAddedListener>()
    private val containerRemovedListeners = mutableSetOf<ContainerRemovedListener>()

    override val size get() = containerMap.size

    private fun getContainerKey(x: Int, y: Int, z: Int): String {
        return "$x;$y;$z"
    }

    override fun getContainer(x: Int, y: Int, z: Int): Container? {
        return containerMap[getContainerKey(x, y, z)]
    }

    override fun addContainer(x: Int, y: Int, z: Int, container: Container) {
        val key = getContainerKey(x, y, z)
        if (containerMap.containsKey(key)) {
            resetContainer(x, y, z)
        }

        containerMap[key] = container

        containerAddedListeners.forEach { listener ->
            listener.onContainerAdded(x, y, z, container)
        }
    }

    private fun retrieveContainer(x: Int, y: Int, z: Int): Container? {
        return containerMap.remove(getContainerKey(x, y, z))
    }

    override fun resetContainer(x: Int, y: Int, z: Int) {
        retrieveContainer(x, y, z)
    }

    override fun destroyContainer(x: Int, y: Int, z: Int) {
        retrieveContainer(x, y, z)?.let { container ->
            containerRemovedListeners.forEach { listener ->
                listener.onContainerRemoved(x, y, z, container)
            }
        }
    }

    override fun addContainerAddedListener(listener: ContainerAddedListener) {
        containerAddedListeners.add(listener)
    }

    override fun removeContainerAddedListener(listener: ContainerAddedListener) {
        containerAddedListeners.remove(listener)
    }

    override fun addContainerRemovedListener(listener: ContainerRemovedListener) {
        containerRemovedListeners.add(listener)
    }

    override fun removeContainerRemovedListener(listener: ContainerRemovedListener) {
        containerRemovedListeners.remove(listener)
    }

    override fun update(delta: Float) {
        containerMap.values.forEach { container ->
            container.update(itemByKey)
        }
    }

    override fun dispose() {
        containerAddedListeners.clear()
        containerRemovedListeners.clear()
        containerMap.clear()
    }
}