package ru.fredboy.cavedroid.gdx.menu.v2.view.stats

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fredboy.cavedroid.common.api.CloudStatsSync
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.stats.model.PlayerStats
import ru.fredboy.cavedroid.domain.stats.model.TrackedLeaderboards
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class StatsMenuViewModel(
    private val navBackStack: NavBackStack,
    private val statsRepository: StatsRepository,
    private val cloudStatsSync: CloudStatsSync,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    private val leaderboardLines = MutableStateFlow<List<String>>(emptyList())

    val stateFlow: StateFlow<StatsMenuState> = combine(
        statsRepository.current,
        leaderboardLines,
    ) { stats, lines ->
        StatsMenuState(
            text = formatStats(stats),
            showLeaderboards = cloudStatsSync.isSupported,
            showSignInHint = cloudStatsSync.isSupported && !cloudStatsSync.isAuthorized,
            leaderboardLines = lines,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500L),
        initialValue = StatsMenuState(""),
    )

    override fun onShow() {
        if (!cloudStatsSync.isSupported || !cloudStatsSync.isAuthorized) {
            leaderboardLines.value = emptyList()
            return
        }
        viewModelScope.launch(ioDispatcher) {
            val rendered = TrackedLeaderboards.ALL.map { entry ->
                val rank = cloudStatsSync.getLeaderboardEntry(entry.name)?.rank
                val rankStr = if (rank != null) "#${rank + 1}" else getLocalizedString("statsLeaderboardUnranked", fallback = "-")
                "${getLocalizedString(entry.labelKey, fallback = entry.name)}: $rankStr"
            }
            leaderboardLines.value = rendered
        }
    }

    fun onBackClick() {
        navBackStack.pop()
    }

    private fun formatStats(stats: PlayerStats): String {
        val mobsTotal = stats.mobsKilled
        val builder = StringBuilder()

        builder.appendLine(line("statsPlayTime", formatDuration(stats.playTimeSec)))
        builder.appendLine(line("statsDaysSurvived", stats.daysSurvivedTotal.toString()))
        builder.appendLine(line("statsBestStreak", stats.bestSurvivalStreakDays.toString()))
        builder.appendLine()
        builder.appendLine(line("statsBlocksPlaced", stats.blocksPlaced.toString()))
        builder.appendLine(line("statsBlocksBroken", stats.blocksBroken.toString()))
        builder.appendLine(line("statsItemsCrafted", stats.itemsCrafted.toString()))
        builder.appendLine(line("statsDistance", stats.distanceWalked.toString()))
        builder.appendLine(line("statsDeepestY", stats.deepestY.toString()))
        builder.appendLine()
        builder.appendLine(line("statsMobsKilled", mobsTotal.toString()))
        stats.mobsKilledByType.entries
            .sortedByDescending { it.value }
            .forEach { (type, count) ->
                builder.appendLine("  ${formatMobType(type)}: $count")
            }
        builder.appendLine()
        builder.appendLine(line("statsDeaths", stats.deaths.toString()))
        builder.appendLine(line("statsDamageDealt", stats.damageDealt.toString()))
        builder.appendLine(line("statsDamageTaken", stats.damageTaken.toString()))

        return builder.toString().trimEnd()
    }

    private fun line(key: String, value: String): String {
        val label = getLocalizedString(key, fallback = key)
        return "$label: $value"
    }

    private fun formatMobType(typeKey: String): String {
        return getLocalizedString("statsMob_$typeKey", fallback = typeKey)
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m ${secs}s"
        } else if (minutes > 0) {
            "${minutes}m ${secs}s"
        } else {
            "${secs}s"
        }
    }
}
