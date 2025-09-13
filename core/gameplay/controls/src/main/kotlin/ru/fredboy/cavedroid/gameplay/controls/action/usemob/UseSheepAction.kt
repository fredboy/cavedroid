package ru.fredboy.cavedroid.gameplay.controls.action.usemob

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.entity.mob.model.SheepMob
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseMobAction
import javax.inject.Inject

@GameScope
@BindUseMobAction(stringKey = UseSheepAction.KEY)
class UseSheepAction @Inject constructor(
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
    private val dropController: DropController,
) : IUseMobAction {

    override fun perform(mob: Mob): Boolean {
        if (mob !is SheepMob || !mob.hasFur || mobController.player.activeItem.amount <= 0) {
            return false
        }

        val used = dyeSheep(mob).takeIf { it } ?: useShears(mob)

        if (used) {
            mobController.player.decreaseCurrentItemCount()
        }

        return used
    }

    private fun dyeSheep(mob: SheepMob): Boolean {
        val woolToColor = SheepMob.FUR_COLORS_MAP[mobController.player.activeItem.item.params.key] ?: return false
        mob.woolToColor = woolToColor
        return true
    }

    private fun useShears(mob: SheepMob): Boolean {
        if (!mobController.player.activeItem.item.isShears()) {
            return false
        }

        mob.hasFur = false
        dropController.addDrop(
            x = mob.position.x,
            y = mob.position.y,
            item = itemsRepository.getItemByKey(mob.woolToColor.first),
            count = (2..3).random(),
        )

        return true
    }

    companion object {
        const val KEY = "sheep"
    }
}
