package ru.fredboy.cavedroid.game.controller.container.listener

import ru.fredboy.cavedroid.game.controller.container.model.Container

fun interface ContainerAddedListener {

    fun onContainerAdded(x: Int, y: Int, z: Int, container: Container)

}
