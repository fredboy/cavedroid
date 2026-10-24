package ru.fredboy.cavedroid.entity.mob.impl

import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.ProjectileAdapter
import ru.fredboy.cavedroid.entity.mob.model.CreeperMob
import kotlin.math.floor

class CreeperMobBehavior : BaseMobBehavior<CreeperMob>(CreeperMob::class) {
    private val pursue = AggressiveMobBehavior(sightGraceSeconds = 0f)
    private val wander = PassiveMobBehavior()

    override fun CreeperMob.updateMob(
        worldAdapter: MobWorldAdapter,
        playerAdapter: PlayerAdapter,
        projectileAdapter: ProjectileAdapter,
        delta: Float,
    ) {
        val target = if (worldAdapter.isInfinite) {
            playerAdapter
        } else {
            object : PlayerAdapter by playerAdapter {
                override val x: Float = playerAdapter.x -
                    floor((playerAdapter.x - position.x + worldAdapter.width / 2f) / worldAdapter.width) * worldAdapter.width
            }
        }
        val distance = position.dst(target.x, target.y)
        val visible = !target.gameMode.isCreative() &&
            distance < CreeperFuse.TRACKING_DISTANCE &&
            canStillTrackPlayer(worldAdapter, target, delta, requireWalkableAngle = false, sightGraceSeconds = 0f)
        fuse.update(distance, visible, target.gameMode.isCreative(), delta)
        if (fuse.isPrimed) {
            controlVector.setZero()
            climb = false
        } else {
            (if (visible) pursue else wander).update(this, worldAdapter, target, projectileAdapter, delta)
        }
    }
}
