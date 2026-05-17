package ru.fredboy.cavedroid.common.api

data class LeaderboardEntry(
    val rank: Int,
    val score: Long,
    val playerName: String?,
)
