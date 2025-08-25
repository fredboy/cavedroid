package ru.fredboy.cavedroid.entity.mob.abstraction

import ru.fredboy.cavedroid.entity.mob.model.Mob

interface MobBehavior {

    val attacksWhenPossible: Boolean get() = false

    fun update(mob: Mob, worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float)
}
