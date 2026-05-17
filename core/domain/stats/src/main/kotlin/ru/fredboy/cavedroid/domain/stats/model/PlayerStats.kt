package ru.fredboy.cavedroid.domain.stats.model

data class PlayerStats(
    val playTimeSec: Long = 0L,
    val daysSurvivedTotal: Long = 0L,
    val bestSurvivalStreakDays: Long = 0L,
    val blocksPlaced: Long = 0L,
    val blocksBroken: Long = 0L,
    val mobsKilled: Long = 0L,
    val mobsKilledByType: Map<String, Long> = emptyMap(),
    val deaths: Long = 0L,
    val distanceWalked: Long = 0L,
    val itemsCrafted: Long = 0L,
    val deepestY: Int = 0,
    val damageDealt: Long = 0L,
    val damageTaken: Long = 0L,
    val lastSubmittedScores: Map<String, Long> = emptyMap(),
) {

    fun toCloudMap(): Map<String, Long> = buildMap {
        put(KEY_PLAY_TIME, playTimeSec)
        put(KEY_DAYS_SURVIVED, daysSurvivedTotal)
        put(KEY_BEST_STREAK, bestSurvivalStreakDays)
        put(KEY_BLOCKS_PLACED, blocksPlaced)
        put(KEY_BLOCKS_BROKEN, blocksBroken)
        put(KEY_MOBS_KILLED, mobsKilled)
        put(KEY_DEATHS, deaths)
        put(KEY_DISTANCE, distanceWalked)
        put(KEY_ITEMS_CRAFTED, itemsCrafted)
        put(KEY_DEEPEST_Y, deepestY.toLong())
        put(KEY_DAMAGE_DEALT, damageDealt)
        put(KEY_DAMAGE_TAKEN, damageTaken)
        mobsKilledByType.forEach { (type, count) ->
            put("$PREFIX_MOB_TYPE$type", count)
        }
    }

    fun mergedWith(remote: Map<String, Long>): PlayerStats {
        val remoteTypeKeys = remote.keys
            .asSequence()
            .filter { it.startsWith(PREFIX_MOB_TYPE) }
            .map { it.removePrefix(PREFIX_MOB_TYPE) }
        val allTypeKeys = (mobsKilledByType.keys.asSequence() + remoteTypeKeys).toSet()
        val mergedByType = allTypeKeys.associateWith { type ->
            maxOf(
                mobsKilledByType[type] ?: 0L,
                remote["$PREFIX_MOB_TYPE$type"] ?: 0L,
            )
        }
        return copy(
            playTimeSec = maxOf(playTimeSec, remote[KEY_PLAY_TIME] ?: 0L),
            daysSurvivedTotal = maxOf(daysSurvivedTotal, remote[KEY_DAYS_SURVIVED] ?: 0L),
            bestSurvivalStreakDays = maxOf(bestSurvivalStreakDays, remote[KEY_BEST_STREAK] ?: 0L),
            blocksPlaced = maxOf(blocksPlaced, remote[KEY_BLOCKS_PLACED] ?: 0L),
            blocksBroken = maxOf(blocksBroken, remote[KEY_BLOCKS_BROKEN] ?: 0L),
            mobsKilled = maxOf(mobsKilled, remote[KEY_MOBS_KILLED] ?: 0L),
            mobsKilledByType = mergedByType,
            deaths = maxOf(deaths, remote[KEY_DEATHS] ?: 0L),
            distanceWalked = maxOf(distanceWalked, remote[KEY_DISTANCE] ?: 0L),
            itemsCrafted = maxOf(itemsCrafted, remote[KEY_ITEMS_CRAFTED] ?: 0L),
            deepestY = maxOf(deepestY, (remote[KEY_DEEPEST_Y] ?: 0L).toInt()),
            damageDealt = maxOf(damageDealt, remote[KEY_DAMAGE_DEALT] ?: 0L),
            damageTaken = maxOf(damageTaken, remote[KEY_DAMAGE_TAKEN] ?: 0L),
        )
    }

    companion object {
        const val KEY_PLAY_TIME = "play_time_sec"
        const val KEY_DAYS_SURVIVED = "days_survived_total"
        const val KEY_BEST_STREAK = "best_survival_streak"
        const val KEY_BLOCKS_PLACED = "blocks_placed"
        const val KEY_BLOCKS_BROKEN = "blocks_broken"
        const val KEY_MOBS_KILLED = "mobs_killed"
        const val KEY_DEATHS = "deaths"
        const val KEY_DISTANCE = "distance_walked"
        const val KEY_ITEMS_CRAFTED = "items_crafted"
        const val KEY_DEEPEST_Y = "deepest_y"
        const val KEY_DAMAGE_DEALT = "damage_dealt"
        const val KEY_DAMAGE_TAKEN = "damage_taken"
        const val PREFIX_MOB_TYPE = "mob_kill_"
    }
}
