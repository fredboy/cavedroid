package ru.fredboy.cavedroid.common.api

interface InventoryHintEvents {

    fun notifyItemMoved()

    fun notifyItemPlacedByOne()

    fun notifyItemHeld()
}
