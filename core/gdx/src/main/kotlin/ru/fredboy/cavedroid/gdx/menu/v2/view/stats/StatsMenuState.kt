package ru.fredboy.cavedroid.gdx.menu.v2.view.stats

data class StatsMenuState(
    val text: String,
    val showLeaderboards: Boolean = false,
    val showSignInHint: Boolean = false,
    val leaderboardLines: List<String> = emptyList(),
)
