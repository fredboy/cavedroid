package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.SheepMob
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob
import kotlin.math.abs

class PassiveMobBehavior :
    BaseMobBehavior<WalkingMob>(
        mobType = WalkingMob::class,
    ) {

    private var targetCoordinates: Pair<Int, Int>? = null

    private fun WalkingMob.getTargetBlock(worldAdapter: MobWorldAdapter): Pair<Int, Int>? {
        val x = ((position.x - WALK_RADIUS).toInt()..(position.x + WALK_RADIUS).toInt()).random()
        var y = position.y.toInt() + 2
        while (y > position.y - 2) {
            if (worldAdapter.getForegroundBlock(x, y).params.hasCollision &&
                !worldAdapter.getForegroundBlock(x, y - 1).params.hasCollision
            ) {
                break
            }
            y--
        }

        if (worldAdapter.getForegroundBlock(x, y - 1).params.hasCollision) {
            return null
        }

        return x to y
    }

    override fun WalkingMob.updateMob(worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        if (checkForAutojump(worldAdapter)) {
            jump()
        }

        if (targetCoordinates == null && (takingDamage || MathUtils.randomBoolean(0.001f))) {
            targetCoordinates = getTargetBlock(worldAdapter)
        }

        targetCoordinates?.let { (targetX, _) ->
            if (abs(targetX - position.x) < 1) {
                targetCoordinates = null
            } else {
                direction = if (targetX > position.x) {
                    Direction.RIGHT
                } else {
                    Direction.LEFT
                }

                controlVector.x = speed * direction.basis
            }
        } ?: run {
            controlVector.setZero()
        }

        climb = canSwim

        if (this is SheepMob && MathUtils.randomBoolean(0.0001f)) {
            hasFur = true
        }
    }

    companion object {
        private const val WALK_RADIUS = 8f
    }
}
