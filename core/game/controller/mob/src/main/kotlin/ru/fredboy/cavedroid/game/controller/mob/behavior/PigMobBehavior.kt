package ru.fredboy.cavedroid.game.controller.mob.behavior

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.entity.mob.model.Pig
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter

class PigMobBehavior : BaseMobBehavior<Pig>(
    mobType = Pig::class,
) {

    override fun Pig.updateMob(worldAdapter: MobWorldAdapter, delta: Float) {
        if (MathUtils.randomBoolean(delta)) {
            if (velocity.x != 0f) {
                velocity.x = 0f
            } else {
                changeDir()
            }
        }
    }

}