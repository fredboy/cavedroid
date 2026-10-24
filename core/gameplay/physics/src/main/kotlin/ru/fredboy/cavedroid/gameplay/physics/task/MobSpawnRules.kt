package ru.fredboy.cavedroid.gameplay.physics.task

import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import kotlin.math.floor

internal object MobSpawnRules {
    const val CREEPER_MIN_PLAYER_DISTANCE = 16f
    const val CREEPER_LOCAL_RADIUS = 96f
    const val CREEPER_LOCAL_CAP = 2

    fun isHostile(type: MobBehaviorType): Boolean = type in setOf(
        MobBehaviorType.AGGRESSIVE,
        MobBehaviorType.ARCHER,
        MobBehaviorType.SPIDER,
        MobBehaviorType.CREEPER,
    )

    fun choose(candidates: List<MobParams>, roll: Float): MobParams? {
        val eligible = candidates.filter { it.spawnWeight > 0f }
        val total = eligible.sumOf { it.spawnWeight.toDouble() }.toFloat()
        if (total <= 0f) return null
        var remaining = roll.coerceIn(0f, 1f) * total
        eligible.forEach {
            remaining -= it.spawnWeight
            if (remaining < 0f) return it
        }
        return eligible.lastOrNull()
    }

    fun distanceSquared(x: Float, y: Float, otherX: Float, otherY: Float, worldWidth: Int, infinite: Boolean): Float {
        var dx = otherX - x
        if (!infinite) dx -= floor((dx + worldWidth / 2f) / worldWidth) * worldWidth
        val dy = otherY - y
        return dx * dx + dy * dy
    }

    fun allowsCreeper(playerDistanceSquared: Float, localCreepers: Int): Boolean = playerDistanceSquared >= CREEPER_MIN_PLAYER_DISTANCE * CREEPER_MIN_PLAYER_DISTANCE && localCreepers < CREEPER_LOCAL_CAP
}
