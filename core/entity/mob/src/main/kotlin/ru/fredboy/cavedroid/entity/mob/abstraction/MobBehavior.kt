package ru.fredboy.cavedroid.entity.mob.abstraction

import ru.fredboy.cavedroid.entity.mob.model.Mob

interface MobBehavior {

    fun update(mob: Mob, worldAdapter: MobWorldAdapter, delta: Float)

}