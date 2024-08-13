package ru.fredboy.cavedroid.game.controller.drop.listener

import ru.fredboy.cavedroid.game.controller.drop.model.Drop

fun interface DropRemovedListener {

    fun onDropRemoved(drop: Drop)

}
