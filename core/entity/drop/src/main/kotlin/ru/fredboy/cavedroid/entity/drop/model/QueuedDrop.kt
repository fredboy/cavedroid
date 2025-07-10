package ru.fredboy.cavedroid.entity.drop.model

import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem

data class QueuedDrop(
    val x: Float,
    val y: Float,
    val item: InventoryItem,
)
