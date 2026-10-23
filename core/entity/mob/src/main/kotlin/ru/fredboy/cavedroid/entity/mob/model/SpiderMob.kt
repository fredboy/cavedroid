package ru.fredboy.cavedroid.entity.mob.model

import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.impl.SpiderMobBehavior
import ru.fredboy.cavedroid.entity.mob.impl.canSpiderClimbWall

class SpiderMob(params: MobParams) : WalkingMob(params) {

    var angerRemainingSec: Float
        get() = (behavior as SpiderMobBehavior).temper.angerRemainingSec
        set(value) {
            (behavior as SpiderMobBehavior).temper.angerRemainingSec = value.coerceAtLeast(0f)
        }

    override fun applyMediumResistanceToBody(mobWorldAdapter: MobWorldAdapter) {
        super.applyMediumResistanceToBody(mobWorldAdapter)
        if (!isFlyMode && !canSwim && canSpiderClimbWall(hitbox, controlVector.x, mobWorldAdapter::getForegroundBlock)) {
            canClimb = true
            body.gravityScale = 0f
            velocity.y = -WALL_CLIMB_SPEED
        }
    }

    companion object {
        private const val WALL_CLIMB_SPEED = 2.5f
    }
}
