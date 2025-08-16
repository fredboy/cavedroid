package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseWaterBucketAction.ACTION_KEY)
class UseWaterBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        gameWorld.placeToForeground(x, y, getBlockByKeyUseCase["water"])
        if (mobsController.player.gameMode != 1) {
            mobsController.player.setCurrentInventorySlotItem(getItemByKeyUseCase["bucket_empty"])
        }
    }

    companion object {
        const val ACTION_KEY = "use_water_bucket"
    }
}
