package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseDoorWoodAction.ACTION_KEY)
class UseDoorWoodAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        val (top, bottom) = if (mobController.player.direction == Direction.RIGHT) {
            getBlockByKeyUseCase["door_wood_top_left_closed"] to getBlockByKeyUseCase["door_wood_bottom_left_closed"]
        } else {
            getBlockByKeyUseCase["door_wood_top_right_closed"] to getBlockByKeyUseCase["door_wood_bottom_right_closed"]
        }

        if (gameWorld.canPlaceToForeground(x, y, bottom) && gameWorld.canPlaceToForeground(x, y - 1, top)) {
            gameWorld.placeToForeground(x, y, bottom)
            gameWorld.placeToForeground(x, y - 1, top)
            mobController.player.decreaseCurrentItemCount()
        }
    }

    companion object {
        const val ACTION_KEY = "use_door_wood_item_action"
    }
}
