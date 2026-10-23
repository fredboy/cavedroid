package ru.fredboy.cavedroid.entity.mob.impl

import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob

class SpiderMobBehavior : BaseMobBehavior<WalkingMob>(WalkingMob::class) {

    val temper = SpiderTemper()

    private var aggressiveBehavior = AggressiveMobBehavior(SIGHT_GRACE_SECONDS)
    private val passiveBehavior = PassiveMobBehavior()

    override var attacksWhenPossible = false
        private set

    override fun onAttackedByPlayer() {
        temper.provoke()
    }

    override fun WalkingMob.updateMob(
        worldAdapter: MobWorldAdapter,
        playerAdapter: PlayerAdapter,
        projectileAdapter: ProjectileAdapter,
        delta: Float,
    ) {
        val aggressive = temper.update(
            isDayTime = worldAdapter.isDayTime(),
            mobY = position.y,
            seaLevel = worldAdapter.seaLevel,
            isCreative = playerAdapter.gameMode.isCreative(),
            delta = delta,
        )
        if (attacksWhenPossible && !aggressive) {
            // Do not retain a pursuit through the next daylight encounter.
            aggressiveBehavior = AggressiveMobBehavior(SIGHT_GRACE_SECONDS)
            controlVector.setZero()
        }
        attacksWhenPossible = aggressive
        val activeBehavior = if (aggressive) aggressiveBehavior else passiveBehavior
        activeBehavior.update(this, worldAdapter, playerAdapter, projectileAdapter, delta)
    }

    companion object {
        // Keep pursuing long enough to climb a wall that briefly hides the player.
        private const val SIGHT_GRACE_SECONDS = 5f
    }
}
