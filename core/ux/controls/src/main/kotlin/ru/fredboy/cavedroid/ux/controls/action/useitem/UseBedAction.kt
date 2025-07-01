package ru.fredboy.cavedroid.ux.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseBedAction.ACTION_KEY)
class UseBedAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        val bedLeft = getBlockByKeyUseCase["bed_l"]
        val bedRight = getBlockByKeyUseCase["bed_r"]

        if (gameWorld.canPlaceToForeground(x, y, bedLeft) && gameWorld.canPlaceToForeground(x + 1, y, bedRight)) {
            gameWorld.placeToForeground(x, y, bedLeft)
            gameWorld.placeToForeground(x + 1, y, bedRight)
            mobController.player.decreaseCurrentItemCount()
        }
    }

    companion object {
        const val ACTION_KEY = "use_bed_action"
    }
}
