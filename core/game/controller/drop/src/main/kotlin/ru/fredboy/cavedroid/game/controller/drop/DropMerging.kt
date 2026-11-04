package ru.fredboy.cavedroid.game.controller.drop

import ru.fredboy.cavedroid.entity.drop.model.Drop
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round

private data class MergeCell(val x: Int, val y: Int, val itemKey: String, val durability: Int)

/** Merges overlapping stacks, returning the emptied drops for disposal outside physics callbacks. */
internal fun mergeNearbyDrops(drops: Iterable<Drop>, worldWidth: Int? = null): Set<Drop> {
    val cells = mutableMapOf<MergeCell, MutableList<Drop>>()
    val consumed = mutableSetOf<Drop>()
    fun cellX(x: Int) = if (worldWidth != null) Math.floorMod(x, worldWidth) else x

    for (drop in drops) {
        if (drop.isPickedUp || drop.amount <= 0 || drop.amount >= drop.item.params.maxStack) continue
        val position = drop.position
        val bounds = drop.hitbox
        val cell = MergeCell(cellX(floor(position.x).toInt()), floor(position.y).toInt(), drop.item.params.key, drop.inventoryItem.durability)

        search@ for (x in cell.x - 1..cell.x + 1) {
            for (y in cell.y - 1..cell.y + 1) {
                val candidates = cells[cell.copy(x = cellX(x), y = y)] ?: continue
                val iterator = candidates.iterator()
                while (iterator.hasNext()) {
                    val target = iterator.next()
                    val targetBounds = target.hitbox
                    if (worldWidth != null) {
                        targetBounds.x += round((bounds.x - targetBounds.x) / worldWidth) * worldWidth
                    }
                    if (!bounds.overlaps(targetBounds)) continue

                    val amount = min(drop.amount, target.item.params.maxStack - target.amount)
                    target.inventoryItem.add(amount)
                    drop.inventoryItem.subtract(amount)
                    if (target.amount == target.item.params.maxStack) iterator.remove()
                    if (drop.amount == 0) {
                        consumed.add(drop)
                        break@search
                    }
                }
            }
        }

        if (drop.amount > 0) cells.getOrPut(cell) { mutableListOf() }.add(drop)
    }
    return consumed
}
