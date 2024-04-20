package ru.deadsoftware.cavedroid.game.actions.placeblock

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class PlaceBlockItemToBackgroundAction @Inject constructor(
    private val gameWorld: GameWorld,
) : IPlaceBlockAction {

    override fun place(placeable: Item.Placeable, x: Int, y: Int) {
        gameWorld.placeToBackground(x, y, placeable.block)
    }

    companion object {
        const val ACTION_KEY = "place_background_block"
    }

}
