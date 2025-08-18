package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.AbstractPassiveMob

class PassiveMobBehavior :
    BaseMobBehavior<AbstractPassiveMob>(
        mobType = AbstractPassiveMob::class,
    ) {

    override fun AbstractPassiveMob.updateMob(worldAdapter: MobWorldAdapter, delta: Float) {
        if (MathUtils.randomBoolean(delta)) {
            if (velocity.x != 0f) {
                velocity.x = 0f
            } else {
                changeDir()
            }
        }

        swim = canSwim
    }
}
