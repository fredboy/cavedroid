package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.domain.items.model.block.Block

data class GameMapSaveData(
    val foreMap: Array<Array<Block>>?,
    val backMap: Array<Array<Block>>?,
    val gameTime: Float,
    val moonPhase: Int,
    val totalGameTime: Float,
    val lastSpawnGameTime: Float,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameMapSaveData

        if (gameTime != other.gameTime) return false
        if (moonPhase != other.moonPhase) return false
        if (!foreMap.contentDeepEquals(other.foreMap)) return false
        if (!backMap.contentDeepEquals(other.backMap)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameTime.hashCode()
        result = 31 * result + moonPhase.hashCode()
        result = 31 * result + (foreMap?.contentDeepHashCode() ?: 0)
        result = 31 * result + (backMap?.contentDeepHashCode() ?: 0)
        return result
    }
}
