package ru.fredboy.cavedroid.common.api

interface CloudStatsSync {

    val isSupported: Boolean

    val isAuthorized: Boolean

    suspend fun loadStats(): Map<String, Long>?

    suspend fun saveStats(stats: Map<String, Long>)

    suspend fun submitLeaderboardScore(name: String, score: Long)

    suspend fun getLeaderboardEntry(name: String): LeaderboardEntry?
}
