package ru.fredboy.cavedroid.entity.drop.model

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem

data class QueuedDrop(
    val x: Float,
    val y: Float,
    val item: InventoryItem,
    val initialForce: Vector2? = null,
)
