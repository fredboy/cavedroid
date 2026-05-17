package ru.fredboy.cavedroid.data.stats.repository

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import ru.fredboy.cavedroid.data.stats.model.PlayerStatsDto
import ru.fredboy.cavedroid.data.stats.model.toDto
import ru.fredboy.cavedroid.data.stats.model.toModel
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.stats.model.PlayerStats
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalSerializationApi::class)
@Singleton
internal class StatsRepositoryImpl @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : StatsRepository {

    private val _current = MutableStateFlow(PlayerStats())
    override val current: StateFlow<PlayerStats> = _current.asStateFlow()

    private val ioMutex = Mutex()

    private fun statsFileHandle(): FileHandle? {
        return try {
            val fileType = applicationContextRepository.getGameDirectoryFileType()
            val path = "${applicationContextRepository.getGameDirectory()}/$STATS_FILE"
            val resolved = if (fileType == Files.FileType.Absolute) path else path.trimStart('/')
            Gdx.files.getFileHandle(resolved, fileType)
        } catch (e: Throwable) {
            logger.w(e) { "Stats file handle resolution failed" }
            null
        }
    }

    override suspend fun load() = ioMutex.withLock {
        val file = statsFileHandle() ?: return@withLock
        if (!file.exists()) return@withLock
        try {
            val bytes = file.readBytes()
            val dto = ProtoBuf.decodeFromByteArray<PlayerStatsDto>(bytes)
            _current.value = dto.toModel()
        } catch (e: Throwable) {
            logger.w(e) { "Failed to load stats.dat — keeping defaults" }
        }
    }

    override suspend fun save() = ioMutex.withLock {
        val file = statsFileHandle() ?: return@withLock
        val snapshot = _current.value
        try {
            val bytes = ProtoBuf.encodeToByteArray(snapshot.toDto())
            file.writeBytes(bytes, false)
        } catch (e: Throwable) {
            logger.w(e) { "Failed to save stats.dat" }
        }
    }

    override fun addPlayTimeSeconds(seconds: Long) {
        if (seconds <= 0) return
        mutate { copy(playTimeSec = playTimeSec + seconds) }
    }

    override fun addDistance(blocks: Long) {
        if (blocks <= 0) return
        mutate { copy(distanceWalked = distanceWalked + blocks) }
    }

    override fun observeDeepestY(y: Int) {
        mutate { copy(deepestY = maxOf(deepestY, y)) }
    }

    override fun recordBlockPlaced() = mutate {
        copy(blocksPlaced = blocksPlaced + 1)
    }

    override fun recordBlockBroken() = mutate {
        copy(blocksBroken = blocksBroken + 1)
    }

    override fun recordMobKilled(typeKey: String) = mutate {
        val updatedByType = mobsKilledByType.toMutableMap().also {
            it[typeKey] = (it[typeKey] ?: 0L) + 1L
        }
        copy(
            mobsKilled = mobsKilled + 1,
            mobsKilledByType = updatedByType,
        )
    }

    override fun recordDeath() = mutate {
        copy(deaths = deaths + 1)
    }

    override fun recordItemCrafted() = mutate {
        copy(itemsCrafted = itemsCrafted + 1)
    }

    override fun recordDamageDealt(amount: Int) {
        val safe = amount.coerceAtLeast(0)
        if (safe == 0) return
        mutate { copy(damageDealt = damageDealt + safe) }
    }

    override fun recordDamageTaken(amount: Int) {
        val safe = amount.coerceAtLeast(0)
        if (safe == 0) return
        mutate { copy(damageTaken = damageTaken + safe) }
    }

    override fun observeDaysSurvived(totalDays: Long, currentStreakDays: Long) = mutate {
        copy(
            daysSurvivedTotal = maxOf(daysSurvivedTotal, totalDays),
            bestSurvivalStreakDays = maxOf(bestSurvivalStreakDays, currentStreakDays),
        )
    }

    override fun updateLastSubmittedScore(leaderboard: String, score: Long) = mutate {
        copy(lastSubmittedScores = lastSubmittedScores + (leaderboard to score))
    }

    override suspend fun mergeFromCloud(remote: Map<String, Long>) {
        _current.update { it.mergedWith(remote) }
    }

    private inline fun mutate(transform: PlayerStats.() -> PlayerStats) {
        _current.update { it.transform() }
    }

    companion object {
        private const val TAG = "StatsRepository"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
        private const val STATS_FILE = "stats.dat"
    }
}
