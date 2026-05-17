package ru.fredboy.cavedroid.data.stats.model

import kotlinx.serialization.Serializable
import ru.fredboy.cavedroid.domain.stats.model.PlayerStats

@Serializable
data class PlayerStatsDto(
    val version: Int = VERSION,
    val playTimeSec: Long = 0L,
    val daysSurvivedTotal: Long = 0L,
    val bestSurvivalStreakDays: Long = 0L,
    val blocksPlaced: Long = 0L,
    val blocksBroken: Long = 0L,
    val mobsKilled: Long = 0L,
    val mobsKilledByType: Map<String, Long> = emptyMap(),
    val deaths: Long = 0L,
    val distanceWalked: Long = 0L,
    val itemsCrafted: Long = 0L,
    val deepestY: Int = 0,
    val damageDealt: Long = 0L,
    val damageTaken: Long = 0L,
    val lastSubmittedScores: Map<String, Long> = emptyMap(),
) {

    companion object {
        const val VERSION = 1
    }
}

fun PlayerStatsDto.toModel(): PlayerStats = PlayerStats(
    playTimeSec = playTimeSec,
    daysSurvivedTotal = daysSurvivedTotal,
    bestSurvivalStreakDays = bestSurvivalStreakDays,
    blocksPlaced = blocksPlaced,
    blocksBroken = blocksBroken,
    mobsKilled = mobsKilled,
    mobsKilledByType = mobsKilledByType,
    deaths = deaths,
    distanceWalked = distanceWalked,
    itemsCrafted = itemsCrafted,
    deepestY = deepestY,
    damageDealt = damageDealt,
    damageTaken = damageTaken,
    lastSubmittedScores = lastSubmittedScores,
)

fun PlayerStats.toDto(): PlayerStatsDto = PlayerStatsDto(
    playTimeSec = playTimeSec,
    daysSurvivedTotal = daysSurvivedTotal,
    bestSurvivalStreakDays = bestSurvivalStreakDays,
    blocksPlaced = blocksPlaced,
    blocksBroken = blocksBroken,
    mobsKilled = mobsKilled,
    mobsKilledByType = mobsKilledByType,
    deaths = deaths,
    distanceWalked = distanceWalked,
    itemsCrafted = itemsCrafted,
    deepestY = deepestY,
    damageDealt = damageDealt,
    damageTaken = damageTaken,
    lastSubmittedScores = lastSubmittedScores,
)
