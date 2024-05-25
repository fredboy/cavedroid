package ru.deadsoftware.cavedroid.game.actions.placeblock

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindPlaceBlockAction
import javax.inject.Inject

@GameScope
@BindPlaceBlockAction(stringKey = PlaceBlockItemToBackgroundAction.ACTION_KEY)
class PlaceBlockItemToBackgroundAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val gameItemsHolder: GameItemsHolder,
    private val mobsController: MobsController,
) : IPlaceBlockAction {

    override fun place(placeable: Item.Placeable, x: Int, y: Int) {
        if (gameWorld.placeToBackground(x, y, placeable.block)) {
            mobsController.player.decreaseCurrentItemCount(gameItemsHolder)
        }
    }

    companion object {
        const val ACTION_KEY = "place_background_block"
    }

}
