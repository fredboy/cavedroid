package ru.fredboy.cavedroid.domain.save.model

import ru.fredboy.cavedroid.common.model.WorldType
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Weather
import ru.fredboy.cavedroid.domain.world.waystone.Waystone

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
    val worldType: WorldType = WorldType.LOOPING,
    val seed: Long? = null,
    val villagesEnabled: Boolean = false,
    val worldSpawnX: Float? = null,
    val worldSpawnY: Float? = null,
    val waystones: List<Waystone> = emptyList(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameMapSaveData

        if (gameTime != other.gameTime) return false
        if (moonPhase != other.moonPhase) return false
        if (worldSpawnX != other.worldSpawnX || worldSpawnY != other.worldSpawnY) return false
        if (waystones != other.waystones) return false
        if (!foreMap.contentDeepEquals(other.foreMap)) return false
        if (!backMap.contentDeepEquals(other.backMap)) return false
        if (!biomes.contentEquals(other.biomes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameTime.hashCode()
        result = 31 * result + moonPhase.hashCode()
        result = 31 * result + (worldSpawnX?.hashCode() ?: 0)
        result = 31 * result + (worldSpawnY?.hashCode() ?: 0)
        result = 31 * result + waystones.hashCode()
        result = 31 * result + (foreMap?.contentDeepHashCode() ?: 0)
        result = 31 * result + (backMap?.contentDeepHashCode() ?: 0)
        result = 31 * result + (biomes?.contentHashCode() ?: 0)
        return result
    }
}
