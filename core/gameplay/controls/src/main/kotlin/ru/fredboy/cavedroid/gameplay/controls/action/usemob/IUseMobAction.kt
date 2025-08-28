package ru.fredboy.cavedroid.gameplay.controls.action.usemob

import ru.fredboy.cavedroid.entity.mob.model.Mob

interface IUseMobAction {

    fun perform(mob: Mob): Boolean
}
