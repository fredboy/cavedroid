package ru.fredboy.cavedroid.gameplay.physics.task

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams

class MobSpawnRulesTest {
    private fun params(key: String, weight: Float): MobParams = mockk {
        every { this@mockk.key } returns key
        every { spawnWeight } returns weight
    }

    @Test
    fun `creepers take about three percent of hostile selections`() {
        val candidates = listOf(params("zombie", 1f), params("skeleton", 1f), params("spider", 1f), params("creeper", 0.08f))
        val counts = (0 until 3080).map { MobSpawnRules.choose(candidates, (it + 0.5f) / 3080)!!.key }.groupingBy { it }.eachCount()
        assertEquals(mapOf("zombie" to 1000, "skeleton" to 1000, "spider" to 1000, "creeper" to 80), counts)
    }

    @Test
    fun `empty and disabled spawn pools do not produce mobs`() {
        assertNull(MobSpawnRules.choose(emptyList(), 0f))
        assertNull(MobSpawnRules.choose(listOf(params("disabled", 0f)), 0.5f))
    }

    @Test
    fun `creepers cannot spawn near players or exceed their local cap`() {
        assertFalse(MobSpawnRules.allowsCreeper(15f * 15f, 0))
        assertTrue(MobSpawnRules.allowsCreeper(16f * 16f, 1))
        assertFalse(MobSpawnRules.allowsCreeper(100f * 100f, 2))
    }

    @Test
    fun `spawn safety distance wraps only in looping worlds`() {
        assertEquals(4f, MobSpawnRules.distanceSquared(255f, 10f, 1f, 10f, 256, false))
        assertEquals(254f * 254f, MobSpawnRules.distanceSquared(255f, 10f, 1f, 10f, 256, true))
    }

    @Test
    fun `creepers are eligible for the hostile pool but passive animals are not`() {
        assertTrue(MobSpawnRules.isHostile(MobBehaviorType.CREEPER))
        assertFalse(MobSpawnRules.isHostile(MobBehaviorType.SHEEP))
        assertFalse(MobSpawnRules.isHostile(MobBehaviorType.PASSIVE))
    }
}
