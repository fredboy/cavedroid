package ru.fredboy.cavedroid.domain.world.listener

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer

fun interface OnBlockPlacedListener {

    fun onBlockPlaced(block: Block, x: Int, y: Int, layer: Layer)

}