package ru.fredboy.cavedroid.domain.stats.repository

import kotlinx.coroutines.flow.StateFlow
import ru.fredboy.cavedroid.domain.stats.model.PlayerStats

interface StatsRepository {

    val current: StateFlow<PlayerStats>

    suspend fun load()

    suspend fun save()

    fun addPlayTimeSeconds(seconds: Long)

    fun addDistance(blocks: Long)

    fun observeDeepestY(y: Int)

    fun recordBlockPlaced()

    fun recordBlockBroken()

    fun recordMobKilled(typeKey: String)

    fun recordDeath()

    fun recordItemCrafted()

    fun recordDamageDealt(amount: Int)

    fun recordDamageTaken(amount: Int)

    fun observeDaysSurvived(totalDays: Long, currentStreakDays: Long)

    fun updateLastSubmittedScore(leaderboard: String, score: Long)

    suspend fun mergeFromCloud(remote: Map<String, Long>)
}
