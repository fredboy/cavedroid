package ru.fredboy.cavedroid.game.world

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome

object WaterFreezing {
    fun canFreeze(
        y: Int,
        biome: Biome,
        maxSurfaceHeight: Int,
        blockAt: (Int) -> Block,
    ): Boolean {
        // Y increases downwards, so maxSurfaceHeight is the lowest possible surface.
        if (biome != Biome.WINTER || y !in 0..maxSurfaceHeight) return false
        val water = blockAt(y) as? Block.Water ?: return false
        if (water.state != 0) return false

        // Require open sky, not just an air pocket inside a shallow cave.
        return (y - 1 downTo 0).all { blockAt(it).isNone() }
    }

    fun freezeSurface(column: Array<Block>, biome: Biome, maxSurfaceHeight: Int, ice: Block) {
        val y = column.indexOfFirst { !it.isNone() }
        if (canFreeze(y, biome, maxSurfaceHeight) { column[it] }) {
            column[y] = ice
        }
    }
}
