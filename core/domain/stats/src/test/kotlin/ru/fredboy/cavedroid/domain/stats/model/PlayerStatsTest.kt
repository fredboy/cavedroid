package ru.fredboy.cavedroid.domain.stats.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlayerStatsTest {

    @Test
    fun `mergedWith takes per-key maximum`() {
        val local = PlayerStats(
            blocksPlaced = 100,
            blocksBroken = 50,
            mobsKilled = 30,
            deaths = 2,
        )
        val remote = mapOf(
            PlayerStats.KEY_BLOCKS_PLACED to 80L,
            PlayerStats.KEY_BLOCKS_BROKEN to 120L,
            PlayerStats.KEY_MOBS_KILLED to 30L,
            PlayerStats.KEY_DEATHS to 5L,
        )

        val merged = local.mergedWith(remote)

        assertEquals(100L, merged.blocksPlaced)
        assertEquals(120L, merged.blocksBroken)
        assertEquals(30L, merged.mobsKilled)
        assertEquals(5L, merged.deaths)
    }

    @Test
    fun `mergedWith preserves untouched fields when remote is empty`() {
        val local = PlayerStats(
            playTimeSec = 3600,
            blocksPlaced = 100,
            bestSurvivalStreakDays = 12,
        )

        val merged = local.mergedWith(emptyMap())

        assertEquals(3600L, merged.playTimeSec)
        assertEquals(100L, merged.blocksPlaced)
        assertEquals(12L, merged.bestSurvivalStreakDays)
    }

    @Test
    fun `mergedWith never decreases values`() {
        val local = PlayerStats(blocksBroken = 500, deepestY = 80)
        val remote = mapOf(
            PlayerStats.KEY_BLOCKS_BROKEN to 100L,
            PlayerStats.KEY_DEEPEST_Y to 40L,
        )

        val merged = local.mergedWith(remote)

        assertEquals(500L, merged.blocksBroken)
        assertEquals(80, merged.deepestY)
    }

    @Test
    fun `mergedWith merges per-type mob kills with max per key`() {
        val local = PlayerStats(
            mobsKilledByType = mapOf("zombie" to 10L, "skeleton" to 5L),
        )
        val remote = mapOf(
            "${PlayerStats.PREFIX_MOB_TYPE}zombie" to 7L,
            "${PlayerStats.PREFIX_MOB_TYPE}skeleton" to 12L,
            "${PlayerStats.PREFIX_MOB_TYPE}cow" to 3L,
        )

        val merged = local.mergedWith(remote)

        assertEquals(10L, merged.mobsKilledByType["zombie"])
        assertEquals(12L, merged.mobsKilledByType["skeleton"])
        assertEquals(3L, merged.mobsKilledByType["cow"])
    }

    @Test
    fun `toCloudMap and mergedWith roundtrip preserves all numeric fields`() {
        val original = PlayerStats(
            playTimeSec = 7200,
            daysSurvivedTotal = 30,
            bestSurvivalStreakDays = 12,
            blocksPlaced = 1234,
            blocksBroken = 4321,
            mobsKilled = 89,
            mobsKilledByType = mapOf("zombie" to 34L, "skeleton" to 21L),
            deaths = 7,
            distanceWalked = 12345,
            itemsCrafted = 145,
            deepestY = 200,
            damageDealt = 999,
            damageTaken = 333,
        )

        val cloudMap = original.toCloudMap()
        val restored = PlayerStats().mergedWith(cloudMap)

        assertEquals(original.playTimeSec, restored.playTimeSec)
        assertEquals(original.daysSurvivedTotal, restored.daysSurvivedTotal)
        assertEquals(original.bestSurvivalStreakDays, restored.bestSurvivalStreakDays)
        assertEquals(original.blocksPlaced, restored.blocksPlaced)
        assertEquals(original.blocksBroken, restored.blocksBroken)
        assertEquals(original.mobsKilled, restored.mobsKilled)
        assertEquals(original.mobsKilledByType, restored.mobsKilledByType)
        assertEquals(original.deaths, restored.deaths)
        assertEquals(original.distanceWalked, restored.distanceWalked)
        assertEquals(original.itemsCrafted, restored.itemsCrafted)
        assertEquals(original.deepestY, restored.deepestY)
        assertEquals(original.damageDealt, restored.damageDealt)
        assertEquals(original.damageTaken, restored.damageTaken)
    }
}
