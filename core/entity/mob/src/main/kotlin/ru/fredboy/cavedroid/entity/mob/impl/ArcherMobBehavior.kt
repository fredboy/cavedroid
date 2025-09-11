package ru.fredboy.cavedroid.entity.mob.impl

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.WalkingMob
import kotlin.math.abs

class ArcherMobBehavior :
    BaseMobBehavior<WalkingMob>(
        mobType = WalkingMob::class,
    ) {

    private val passiveBehavior = PassiveMobBehavior()

    override val attacksWhenPossible: Boolean
        get() = true

    override fun WalkingMob.updateMob(
        worldAdapter: MobWorldAdapter,
        playerAdapter: PlayerAdapter,
        projectileAdapter: ProjectileAdapter,
        delta: Float,
    ) {
        if (playerAdapter.gameMode.isCreative() || position.dst(playerAdapter.x, playerAdapter.y) > TRIGGER_DISTANCE) {
            passiveBehavior.update(this, worldAdapter, playerAdapter, projectileAdapter, delta)
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

        if (abs(position.x - playerAdapter.x) <= SHOOTING_RANGE && abs(position.y - playerAdapter.y) <= 1f) {
            controlVector.x = 0f
            isPullingBow = true
        } else {
            isPullingBow = false
        }

        if (bowCharge > 2f) {
            projectileAdapter.addProjectile(
                itemKey = "arrow",
                damage = 4,
                dropOnGround = MathUtils.randomBoolean(0.1f),
                x = position.x + direction.basis,
                y = position.y - height / 3f,
                width = 1f,
                height = 0.25f,
                velocity = Vector2(300f * direction.basis, 0f),
            )
            isPullingBow = false
        }
    }

    companion object {
        private const val TRIGGER_DISTANCE = 16f
        private const val SHOOTING_RANGE = 4f
    }
}
