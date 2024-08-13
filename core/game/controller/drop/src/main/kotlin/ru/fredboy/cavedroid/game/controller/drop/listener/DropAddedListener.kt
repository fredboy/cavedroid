package ru.fredboy.cavedroid.game.controller.drop.listener

import ru.fredboy.cavedroid.game.controller.drop.model.Drop

fun interface DropAddedListener {

    fun onDropAdded(drop: Drop)

}