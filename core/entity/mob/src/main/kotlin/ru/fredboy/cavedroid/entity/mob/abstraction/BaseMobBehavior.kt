package ru.fredboy.cavedroid.entity.mob.abstraction

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.RayCastCallback
import ru.fredboy.cavedroid.common.utils.ceil
import ru.fredboy.cavedroid.common.utils.floor
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Mob
import kotlin.math.abs
import kotlin.reflect.KClass

abstract class BaseMobBehavior<MOB : Mob>(
    val mobType: KClass<MOB>,
) : MobBehavior {

    private val losStart = Vector2()
    private val losEnd = Vector2()
    private var losBlocked = false
    private val losCallback = RayCastCallback { fixture, _, _, _ ->
        if (fixture.filterData.categoryBits.toInt() and PhysicsConstants.CATEGORY_BLOCK.toInt() != 0) {
            losBlocked = true
            0f
        } else {
            -1f
        }
    }

    protected var lostSightElapsedSec: Float = Float.MAX_VALUE

    @Suppress("UNCHECKED_CAST")
    final override fun update(mob: Mob, worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, projectileAdapter: ProjectileAdapter, delta: Float) {
        if (mobType.isInstance(mob)) {
            with(mob as MOB) {
                updateMob(worldAdapter, playerAdapter, projectileAdapter, delta)
            }
        } else {
            throw IllegalArgumentException(
                "Trying to update mob of type ${mob::class.simpleName} with behavior of ${mobType.simpleName}",
            )
        }
    }

    open fun MOB.updateMob(worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, projectileAdapter: ProjectileAdapter, delta: Float) {
        if (checkForAutojump(worldAdapter)) {
            jump()
        }
    }

    open fun MOB.checkForAutojump(worldAdapter: MobWorldAdapter): Boolean {
        return controlVector.x != 0f &&
            autojumpCounters[Direction.fromVector(controlVector).index] > 0 &&
            checkAutojumpObstacle(worldAdapter)
    }

    protected fun MOB.checkAutojumpObstacle(worldAdapter: MobWorldAdapter): Boolean {
        val targetHitbox = hitbox.apply {
            val dir = Direction.fromVector(controlVector)
            width = 1f
            x = when (dir) {
                Direction.LEFT -> position.x.floor
                Direction.RIGHT -> position.x.ceil
            } - width / 2f
            y -= 1f
        }

        var hasObstacle = false
        forEachBlockInArea(targetHitbox) { x, y ->
            val block = worldAdapter.getForegroundBlock(x, y)
            if (block.params.hasCollision && targetHitbox.overlaps(block.getRectangle(x, y))) {
                hasObstacle = true
            }
        }
        return !hasObstacle
    }

    protected fun MOB.hasLineOfSightTo(
        worldAdapter: MobWorldAdapter,
        targetX: Float,
        targetY: Float,
    ): Boolean {
        losStart.set(position.x, position.y)
        losEnd.set(targetX, targetY)
        if (losStart.epsilonEquals(losEnd)) {
            return true
        }
        losBlocked = false
        worldAdapter.getBox2dWorld().rayCast(losCallback, losStart, losEnd)
        return !losBlocked
    }

    protected fun MOB.canStillTrackPlayer(
        worldAdapter: MobWorldAdapter,
        playerAdapter: PlayerAdapter,
        delta: Float,
        requireWalkableAngle: Boolean,
    ): Boolean {
        val dx = playerAdapter.x - position.x
        val dy = playerAdapter.y - position.y
        val angleOk = !requireWalkableAngle || abs(dx) < 0.01 || abs(dy / dx) < 1.732f // arctan(60)
        val reachable = angleOk && hasLineOfSightTo(worldAdapter, playerAdapter.x, playerAdapter.y)

        if (reachable) {
            lostSightElapsedSec = 0f
            return true
        }
        lostSightElapsedSec += delta
        return lostSightElapsedSec < SIGHT_GRACE_SECONDS
    }

    companion object {
        private const val TAG = "BaseMobBehavior"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val SIGHT_GRACE_SECONDS = 1f
    }
}
