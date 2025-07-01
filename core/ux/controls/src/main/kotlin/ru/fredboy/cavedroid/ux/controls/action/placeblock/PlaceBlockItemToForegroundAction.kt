package ru.fredboy.cavedroid.ux.controls.action.placeblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.controls.action.annotation.BindPlaceBlockAction
import javax.inject.Inject

@GameScope
@BindPlaceBlockAction(stringKey = PlaceBlockItemToForegroundAction.ACTION_KEY)
class PlaceBlockItemToForegroundAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val placeSlabAction: PlaceSlabAction,
    private val mobController: MobController,
) : IPlaceBlockAction {

    override fun place(placeable: Item.Placeable, x: Int, y: Int) {
        if (placeable.isSlab()) {
            placeSlabAction.place(placeable, x, y)
        } else {
            if (gameWorld.placeToForeground(x, y, placeable.block)) {
                mobController.player.decreaseCurrentItemCount()
            }
        }
    }

    companion object {
        const val ACTION_KEY = "place_foreground_block"
    }
}
