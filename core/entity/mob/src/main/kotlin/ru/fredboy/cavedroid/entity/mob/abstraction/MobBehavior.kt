package ru.fredboy.cavedroid.entity.mob.abstraction

import ru.fredboy.cavedroid.entity.mob.model.Mob

interface MobBehavior {

    val attacksWhenPossible: Boolean get() = false

    fun onAttackedByPlayer() = Unit

    fun update(mob: Mob, worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, projectileAdapter: ProjectileAdapter, delta: Float)
}
