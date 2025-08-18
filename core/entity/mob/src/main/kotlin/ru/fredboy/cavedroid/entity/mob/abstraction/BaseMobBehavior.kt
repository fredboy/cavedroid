package ru.fredboy.cavedroid.entity.mob.abstraction

import ru.fredboy.cavedroid.entity.mob.model.Mob
import kotlin.reflect.KClass

abstract class BaseMobBehavior<MOB : Mob>(
    val mobType: KClass<MOB>,
) : MobBehavior {

    @Suppress("UNCHECKED_CAST")
    final override fun update(mob: Mob, worldAdapter: MobWorldAdapter, delta: Float) {
        if (mobType.isInstance(mob)) {
            with(mob as MOB) {
                updateMob(worldAdapter, delta)
            }
        } else {
            throw IllegalArgumentException(
                "Trying to update mob of type ${mob::class.simpleName} with behavior of ${mobType.simpleName}",
            )
        }
    }

    abstract fun MOB.updateMob(worldAdapter: MobWorldAdapter, delta: Float)

    companion object {
        private const val TAG = "BaseMobBehavior"
    }
}
