package ru.fredboy.cavedroid.game.controller.stats

import com.badlogic.gdx.utils.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.fredboy.cavedroid.common.api.CloudStatsSync
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.stats.model.TrackedLeaderboards
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject
import kotlin.math.abs

@GameScope
class StatsController @Inject constructor(
    private val statsRepository: StatsRepository,
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val cloudStatsSync: CloudStatsSync,
    dispatchers: AppDispatchers,
) : Disposable {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private val blockDestroyedListener = OnBlockDestroyedListener { _, _, _, _, _, destroyedByPlayer ->
        if (destroyedByPlayer) {
            statsRepository.recordBlockBroken()
        }
    }

    private val mobAttackedCallback: (Mob, Int) -> Unit = { mob, damage ->
        if (damage > 0) {
            statsRepository.recordDamageDealt(damage)
        }
        if (mob.isDead) {
            statsRepository.recordMobKilled(mob.params.key)
        }
    }

    private var playTimeAccum: Float = 0f
    private var distanceAccum: Float = 0f
    private var lastPlayerPosX: Float = Float.NaN
    private var lastDayIndex: Int = currentDayIndex()
    private var lastIsDead: Boolean = mobController.player.isDead
    private var lastHealth: Int = mobController.player.health

    init {
        gameWorld.addBlockDestroyedListener(blockDestroyedListener)
        mobController.player.onMobAttacked = mobAttackedCallback
    }

    fun update(delta: Float) {
        val player = mobController.player

        playTimeAccum += delta
        if (playTimeAccum >= 1f) {
            val whole = playTimeAccum.toLong()
            playTimeAccum -= whole.toFloat()
            statsRepository.addPlayTimeSeconds(whole)
        }

        val px = player.position.x
        if (!lastPlayerPosX.isNaN()) {
            val dx = abs(px - lastPlayerPosX)
            // Filter out horizontal world-wrap jumps (world is toroidal).
            if (dx < gameWorld.width / 2f) {
                distanceAccum += dx
                if (distanceAccum >= 1f) {
                    val whole = distanceAccum.toLong()
                    distanceAccum -= whole.toFloat()
                    statsRepository.addDistance(whole)
                }
            }
        }
        lastPlayerPosX = px

        statsRepository.observeDeepestY(player.position.y.toInt())

        if (lastHealth >= 0 && player.health < lastHealth) {
            statsRepository.recordDamageTaken(lastHealth - player.health)
        }
        lastHealth = player.health

        if (player.isDead && !lastIsDead) {
            val currentDay = currentDayIndex()
            val streakDays = (currentDay - gameWorld.currentStreakStartDayIndex).coerceAtLeast(0)
            statsRepository.observeDaysSurvived(
                totalDays = currentDay.toLong(),
                currentStreakDays = streakDays.toLong(),
            )
            statsRepository.recordDeath()
            gameWorld.currentStreakStartDayIndex = currentDay
        } else if (!player.isDead && lastIsDead) {
            // Respawn: avoid counting the teleport as walked distance.
            lastPlayerPosX = Float.NaN
        }
        lastIsDead = player.isDead

        val today = currentDayIndex()
        if (today > lastDayIndex) {
            val streakDays = (today - gameWorld.currentStreakStartDayIndex).coerceAtLeast(0)
            statsRepository.observeDaysSurvived(
                totalDays = today.toLong(),
                currentStreakDays = streakDays.toLong(),
            )
            lastDayIndex = today
        }
    }

    fun onSaveCheckpoint() {
        scope.launch {
            statsRepository.save()
            if (cloudStatsSync.isSupported && cloudStatsSync.isAuthorized) {
                pushCloudAndLeaderboards()
            }
        }
    }

    private suspend fun pushCloudAndLeaderboards() {
        val snapshot = statsRepository.current.value
        runCatching { cloudStatsSync.saveStats(snapshot.toCloudMap()) }
            .onFailure { logger.w(it) { "Cloud stats push failed" } }

        for (entry in TrackedLeaderboards.ALL) {
            val current = entry.scorer(snapshot)
            val lastSubmitted = snapshot.lastSubmittedScores[entry.name] ?: 0L
            if (current > lastSubmitted) {
                runCatching { cloudStatsSync.submitLeaderboardScore(entry.name, current) }
                    .onFailure { logger.w(it) { "Leaderboard ${entry.name} submit failed" } }
                statsRepository.updateLastSubmittedScore(entry.name, current)
            }
        }
    }

    private fun currentDayIndex(): Int {
        return (gameWorld.totalGameTimeSec / GameWorld.DAY_DURATION_SEC).toInt()
    }

    override fun dispose() {
        gameWorld.removeBlockDestroyedListener(blockDestroyedListener)
        if (mobController.player.onMobAttacked === mobAttackedCallback) {
            mobController.player.onMobAttacked = null
        }
        scope.cancel()
    }

    companion object {
        private const val TAG = "StatsController"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
