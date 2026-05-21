package ru.fredboy.cavedroid.game.window.inventory

abstract class AbstractInventoryWindowWithScroll : AbstractInventoryWindow() {

    abstract fun getMaxScroll(): Int
}
