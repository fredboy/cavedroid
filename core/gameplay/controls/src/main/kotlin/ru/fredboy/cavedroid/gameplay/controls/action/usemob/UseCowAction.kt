package ru.fredboy.cavedroid.gameplay.controls.action.usemob

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseMobAction
import javax.inject.Inject

@GameScope
@BindUseMobAction(stringKey = UseCowAction.KEY)
class UseCowAction @Inject constructor(
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
) : IUseMobAction {

    override fun perform(mob: Mob): Boolean {
        if (mobController.player.activeItem.item.params.key == "bucket_empty") {
            mobController.player.apply {
                inventory.items[activeSlot] = InventoryItem(itemsRepository.getItemByKey("bucket_milk"))
            }
            return true
        }

        return false
    }

    companion object {
        const val KEY = "cow"
    }
}
