package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.domain.items.model.block.Block

class GameMapSaveData(
    private var foreMap: Array<Array<Block>>?,
    private var backMap: Array<Array<Block>>?,
) {
    fun retrieveForeMap(): Array<Array<Block>> {
        val value = requireNotNull(foreMap)
        foreMap = null
        return value
    }

    fun retrieveBackMap(): Array<Array<Block>> {
        val value = requireNotNull(backMap)
        backMap = null
        return value
    }

    fun isEmpty() = foreMap == null && backMap == null
}
