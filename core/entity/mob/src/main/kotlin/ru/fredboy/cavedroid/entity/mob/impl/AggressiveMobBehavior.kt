package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob

class AggressiveMobBehavior :
    BaseMobBehavior<WalkingMob>(
        mobType = WalkingMob::class,
    ) {

    override val attacksWhenPossible: Boolean
        get() = true

    override fun WalkingMob.updateMob(worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        if (checkForAutojump()) {
            jump()
        }

        if (position.dst(playerAdapter.x, playerAdapter.y) <= TRIGGER_DISTANCE) {
            direction = if (playerAdapter.x > position.x) {
                Direction.RIGHT
            } else {
                Direction.LEFT
            }

            controlVector.x = speed * direction.basis
        } else if (MathUtils.randomBoolean(delta)) {
            if (velocity.x != 0f) {
                velocity.x = 0f
            } else {
                changeDir()
            }
        }

        climb = canSwim
    }

    companion object {
        private const val TRIGGER_DISTANCE = 16f
    }
}
