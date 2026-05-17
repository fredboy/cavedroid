package ru.fredboy.cavedroid.html

import co.touchlab.kermit.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import ru.fredboy.cavedroid.common.api.CloudStatsSync
import ru.fredboy.cavedroid.common.api.LeaderboardEntry
import kotlin.coroutines.resume

class YandexCloudStatsSync : CloudStatsSync {

    override val isSupported: Boolean = true

    @Volatile
    private var playerReady = false

    override val isAuthorized: Boolean
        get() = playerReady && safe { YandexGamesBridge.isPlayerAuthorized() } == true

    private suspend fun ensurePlayer() {
        if (playerReady) return
        suspendCancellableCoroutine<Unit> { cont ->
            try {
                YandexGamesBridge.initPlayer {
                    playerReady = true
                    if (cont.isActive) cont.resume(Unit)
                }
            } catch (e: Throwable) {
                logger.w(e) { "initPlayer() bridge call failed" }
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    override suspend fun loadStats(): Map<String, Long>? {
        ensurePlayer()
        if (!isAuthorized) return null
        val raw = suspendCancellableCoroutine<String?> { cont ->
            try {
                YandexGamesBridge.getStats { json ->
                    if (cont.isActive) cont.resume(json)
                }
            } catch (e: Throwable) {
                logger.w(e) { "getStats() bridge call failed" }
                if (cont.isActive) cont.resume(null)
            }
        } ?: return null
        return parseLongMap(raw)
    }

    override suspend fun saveStats(stats: Map<String, Long>) {
        ensurePlayer()
        if (!isAuthorized) return
        val json = encodeLongMap(stats)
        try {
            YandexGamesBridge.setStats(json)
        } catch (e: Throwable) {
            logger.w(e) { "setStats() bridge call failed" }
        }
    }

    override suspend fun submitLeaderboardScore(name: String, score: Long) {
        ensurePlayer()
        if (!isAuthorized) return
        try {
            YandexGamesBridge.setLeaderboardScore(name, score.toDouble())
        } catch (e: Throwable) {
            logger.w(e) { "setLeaderboardScore($name) bridge call failed" }
        }
    }

    override suspend fun getLeaderboardEntry(name: String): LeaderboardEntry? {
        ensurePlayer()
        val raw = suspendCancellableCoroutine<String?> { cont ->
            try {
                YandexGamesBridge.getLeaderboardPlayerEntry(name) { json ->
                    if (cont.isActive) cont.resume(json)
                }
            } catch (e: Throwable) {
                logger.w(e) { "getLeaderboardPlayerEntry($name) bridge call failed" }
                if (cont.isActive) cont.resume(null)
            }
        } ?: return null
        return parseLeaderboardEntry(raw)
    }

    private fun parseLongMap(json: String): Map<String, Long>? {
        if (json.isBlank() || json == "null") return null
        return try {
            val obj = Json.parseToJsonElement(json) as? JsonObject ?: return null
            obj.entries.mapNotNull { (key, element) ->
                val primitive = element as? JsonPrimitive ?: return@mapNotNull null
                val value = primitive.longOrNull ?: primitive.doubleOrNull?.toLong() ?: return@mapNotNull null
                key to value
            }.toMap()
        } catch (e: Throwable) {
            logger.w(e) { "parseLongMap() failed: $json" }
            null
        }
    }

    private fun encodeLongMap(stats: Map<String, Long>): String {
        return stats.entries.joinToString(separator = ",", prefix = "{", postfix = "}") { (k, v) ->
            "\"${escape(k)}\":$v"
        }
    }

    private fun escape(s: String): String {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
    }

    private fun parseLeaderboardEntry(json: String): LeaderboardEntry? {
        if (json.isBlank() || json == "null") return null
        return try {
            val obj = Json.parseToJsonElement(json) as? JsonObject ?: return null
            val rank = (obj["rank"] as? JsonPrimitive)?.longOrNull?.toInt() ?: return null
            val score = (obj["score"] as? JsonPrimitive)?.longOrNull
                ?: (obj["score"] as? JsonPrimitive)?.doubleOrNull?.toLong()
                ?: return null
            val playerName = obj["player"]?.jsonObject?.get("publicName")?.jsonPrimitive?.contentOrNull
            LeaderboardEntry(rank = rank, score = score, playerName = playerName)
        } catch (e: Throwable) {
            logger.w(e) { "parseLeaderboardEntry() failed: $json" }
            null
        }
    }

    private inline fun <T> safe(block: () -> T): T? {
        return try {
            block()
        } catch (e: Throwable) {
            logger.w(e) { "Yandex bridge call failed" }
            null
        }
    }

    companion object {
        private const val TAG = "YandexCloudStatsSync"
        private val logger = Logger.withTag(TAG)
    }
}
