package ru.fredboy.cavedroid.game.controller.container.listener

import ru.fredboy.cavedroid.game.controller.container.model.Container

fun interface ContainerRemovedListener {

    fun onContainerRemoved(x: Int, y: Int, z: Int, container: Container)

}
