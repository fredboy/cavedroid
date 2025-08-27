package ru.fredboy.cavedroid.domain.world.listener

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer

fun interface OnBlockDestroyedListener {

    fun onBlockDestroyed(block: Block, x: Int, y: Int, layer: Layer, withDrop: Boolean, destroyedByPlayer: Boolean)
}
