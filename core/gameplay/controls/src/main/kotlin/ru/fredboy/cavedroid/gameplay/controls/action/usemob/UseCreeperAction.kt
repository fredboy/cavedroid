package ru.fredboy.cavedroid.gameplay.controls.action.usemob

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.entity.mob.model.CreeperMob
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseMobAction
import javax.inject.Inject

@GameScope
@BindUseMobAction("creeper")
class UseCreeperAction @Inject constructor(private val mobs: MobController) : IUseMobAction {
    override fun perform(mob: Mob): Boolean {
        val creeper = mob as? CreeperMob ?: return false
        val item = mobs.player.activeItem
        if (creeper.isDead || item.item !is Item.FlintAndSteel || item.amount <= 0 || item.durability <= 0) return false
        if (!creeper.fuse.forced) {
            creeper.fuse.forced = true
            mobs.player.durateActiveDurable()
        }
        return true
    }
}
