package ru.fredboy.cavedroid.entity.drop.abstraction

import ru.fredboy.cavedroid.domain.items.model.inventory.Inventory

interface DropAdapter {

    fun dropInventory(x: Float, y: Float, inventory: Inventory)

}