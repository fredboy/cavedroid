package ru.fredboy.cavedroid.common.api

class NoOpCloudStatsSync : CloudStatsSync {

    override val isSupported: Boolean = false

    override val isAuthorized: Boolean = false

    override suspend fun loadStats(): Map<String, Long>? = null

    override suspend fun saveStats(stats: Map<String, Long>) = Unit

    override suspend fun submitLeaderboardScore(name: String, score: Long) = Unit

    override suspend fun getLeaderboardEntry(name: String): LeaderboardEntry? = null
}
