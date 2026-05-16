package ru.fredboy.cavedroid.game.controller.container

fun interface FurnaceStateChangedListener {

    fun onFurnaceStateChanged(x: Int, y: Int, z: Int, isActive: Boolean)
}
