package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseCakeAction.ACTION_KEY)
class UseCakeAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val cake = getBlockByKeyUseCase["cake_0"]

        if (gameWorld.canPlaceToForeground(x, y, cake) && gameWorld.getForeMap(x, y + 1).params.hasCollision) {
            gameWorld.placeToForeground(x, y, cake)
            mobController.player.decreaseCurrentItemCount()
            return true
        }

        return false
    }

    companion object {
        const val ACTION_KEY = "use_cake_action"
    }
}
