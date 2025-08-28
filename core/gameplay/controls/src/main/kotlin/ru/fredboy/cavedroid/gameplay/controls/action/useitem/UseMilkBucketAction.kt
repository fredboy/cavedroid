package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseMilkBucketAction.ACTION_KEY)
class UseMilkBucketAction @Inject constructor(
    private val mobController: MobController,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        if (mobController.player.health < mobController.player.maxHealth) {
            mobController.player.heal(HEAL)
            mobController.player.inventory.items[mobController.player.activeSlot] = getItemByKeyUseCase["bucket_empty"]
                .toInventoryItem()
            return true
        }

        return false
    }

    companion object {
        private const val HEAL = 6
        const val ACTION_KEY = "use_milk_bucket"
    }
}
