package ru.fredboy.cavedroid.game.controller.container

import ru.fredboy.cavedroid.game.controller.container.listener.ContainerAddedListener
import ru.fredboy.cavedroid.game.controller.container.listener.ContainerRemovedListener
import ru.fredboy.cavedroid.game.controller.container.model.Container

interface ContainerController {

    val size: Int

    fun getContainer(x: Int, y: Int, z: Int): Container?

    fun addContainer(x: Int, y: Int, z: Int, container: Container)

    /**
     * Removes container without notifying listeners
     */
    fun resetContainer(x: Int, y: Int, z: Int)

    /**
     * Removes container and notifies listeners
     */
    fun destroyContainer(x: Int, y: Int, z: Int)

    fun addContainerAddedListener(listener: ContainerAddedListener)

    fun removeContainerAddedListener(listener: ContainerAddedListener)

    fun addContainerRemovedListener(listener: ContainerRemovedListener)

    fun removeContainerRemovedListener(listener: ContainerRemovedListener)

    fun update(delta: Float)

    fun dispose()

}