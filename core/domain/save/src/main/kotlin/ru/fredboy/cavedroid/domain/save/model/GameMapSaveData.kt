package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Weather

data class GameMapSaveData(
    val foreMap: Array<Array<Block>>?,
    val backMap: Array<Array<Block>>?,
    val biomes: Array<Biome>?,
    val gameTime: Float,
    val moonPhase: Int,
    val totalGameTime: Float,
    val lastSpawnGameTime: Float,
    val weather: Weather?,
    val weatherTimer: Float?,
    val weatherIntensity: Float?,
    val currentStreakStartDayIndex: Int = 0,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameMapSaveData

        if (gameTime != other.gameTime) return false
        if (moonPhase != other.moonPhase) return false
        if (!foreMap.contentDeepEquals(other.foreMap)) return false
        if (!backMap.contentDeepEquals(other.backMap)) return false
        if (!biomes.contentEquals(other.biomes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameTime.hashCode()
        result = 31 * result + moonPhase.hashCode()
        result = 31 * result + (foreMap?.contentDeepHashCode() ?: 0)
        result = 31 * result + (backMap?.contentDeepHashCode() ?: 0)
        result = 31 * result + (biomes?.contentHashCode() ?: 0)
        return result
    }
}
