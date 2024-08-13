package ru.fredboy.cavedroid.game.controller.mob

import ru.fredboy.cavedroid.game.controller.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.model.Player

interface MobController {

    val mobs: List<Mob>

    val player: Player

    fun addMob(mob: Mob)

    fun removeMob(mob: Mob)

    operator fun plusAssign(mob: Mob) {
        addMob(mob)
    }

    operator fun minusAssign(mob: Mob) {
        removeMob(mob)
    }

}