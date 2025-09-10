package ru.fredboy.cavedroid.entity.mob.impl

import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob

class AggressiveMobBehavior :
    BaseMobBehavior<WalkingMob>(
        mobType = WalkingMob::class,
    ) {

    private val passiveBehavior = PassiveMobBehavior()

    override val attacksWhenPossible: Boolean
        get() = true

    override fun WalkingMob.updateMob(worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        if (playerAdapter.gameMode.isCreative() || position.dst(playerAdapter.x, playerAdapter.y) > TRIGGER_DISTANCE) {
            passiveBehavior.update(this, worldAdapter, playerAdapter, delta)
            return
        }

        if (checkForAutojump(worldAdapter)) {
            jump()
        }

        direction = if (playerAdapter.x > position.x) {
            Direction.RIGHT
        } else {
            Direction.LEFT
        }

        controlVector.x = speed * direction.basis

        if (!canClimb && controlVector.x != 0f && cliffEdgeCounters[Direction.fromVector(controlVector).index] <= 0) {
            controlVector.x = 0f
        }

        climb = canSwim
    }

    companion object {
        private const val TRIGGER_DISTANCE = 16f
    }
}
