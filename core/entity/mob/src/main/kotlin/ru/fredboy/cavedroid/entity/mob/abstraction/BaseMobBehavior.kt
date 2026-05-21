package ru.fredboy.cavedroid.entity.mob.abstraction

import ru.fredboy.cavedroid.common.utils.PIXELS_PER_METER
import ru.fredboy.cavedroid.common.utils.floor
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.common.utils.pixels
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Mob
import kotlin.reflect.KClass

abstract class BaseMobBehavior<MOB : Mob>(
    val mobType: KClass<MOB>,
) : MobBehavior {

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
            x = ((x + width / 2f) + dir.basis).floor + when (dir) {
                Direction.LEFT -> (PIXELS_PER_METER - 1).meters
                Direction.RIGHT -> 1f.meters
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

    companion object {
        private const val TAG = "BaseMobBehavior"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
