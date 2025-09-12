package ru.fredboy.cavedroid.entity.mob.abstraction

import ru.fredboy.cavedroid.entity.mob.model.Mob

interface MobFactory {

    fun create(x: Float, y: Float, mobKey: String): Mob?
}
