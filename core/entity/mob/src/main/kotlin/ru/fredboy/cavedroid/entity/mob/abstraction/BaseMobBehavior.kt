package ru.fredboy.cavedroid.entity.mob.abstraction

import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.entity.mob.model.Mob
import kotlin.reflect.KClass

abstract class BaseMobBehavior<MOB : Mob>(
    val mobType: KClass<MOB>,
) : MobBehavior {

    @Suppress("UNCHECKED_CAST")
    final override fun update(mob: Mob, worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        if (mobType.isInstance(mob)) {
            with(mob as MOB) {
                updateMob(worldAdapter, playerAdapter, delta)
            }
        } else {
            throw IllegalArgumentException(
                "Trying to update mob of type ${mob::class.simpleName} with behavior of ${mobType.simpleName}",
            )
        }
    }

    open fun MOB.updateMob(worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        if (checkForAutojump(worldAdapter)) {
            jump()
        }
    }

    open fun MOB.checkForAutojump(worldAdapter: MobWorldAdapter): Boolean {
        return controlVector.x != 0f &&
            autojumpCounters[Direction.fromVector(controlVector).index] > 0 &&
            checkAutojumpObstacle(worldAdapter)
    }

    private fun MOB.checkAutojumpObstacle(worldAdapter: MobWorldAdapter): Boolean {
        val targetHitbox = hitbox.apply {
            x += Direction.fromVector(controlVector).basis
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
    }
}
