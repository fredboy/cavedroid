package ru.fredboy.cavedroid.domain.stats.model

data class TrackedLeaderboard(
    val name: String,
    val labelKey: String,
    val scorer: (PlayerStats) -> Long,
)

object TrackedLeaderboards {

    val ALL: List<TrackedLeaderboard> = listOf(
        TrackedLeaderboard(
            name = "mobsKilled",
            labelKey = "statsLeaderboardMobsKilled",
            scorer = PlayerStats::mobsKilled,
        ),
        TrackedLeaderboard(
            name = "blocksBroken",
            labelKey = "statsLeaderboardBlocksBroken",
            scorer = PlayerStats::blocksBroken,
        ),
        TrackedLeaderboard(
            name = "blocksPlaced",
            labelKey = "statsLeaderboardBlocksPlaced",
            scorer = PlayerStats::blocksPlaced,
        ),
        TrackedLeaderboard(
            name = "bestSurvivalStreak",
            labelKey = "statsLeaderboardBestStreak",
            scorer = PlayerStats::bestSurvivalStreakDays,
        ),
    )
}
