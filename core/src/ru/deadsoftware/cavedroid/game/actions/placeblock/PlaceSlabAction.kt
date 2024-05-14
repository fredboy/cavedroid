package ru.deadsoftware.cavedroid.game.actions.placeblock

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@PlaceBlockAction(stringKey = PlaceSlabAction.ACTION_KEY)
class PlaceSlabAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IPlaceBlockAction {

    override fun place(placeable: Item.Placeable, x: Int, y: Int) {
        if (placeable !is Item.Slab) {
            Gdx.app.debug(TAG, "Place slab action called on ${placeable.params.key} which is not a slab")
            return
        }

        val slabPart = if ((gameWorld.hasForeAt(x, y - 1)
                    || gameWorld.getForeMap(x - 1, y) == placeable.topPartBlock
                    || gameWorld.getForeMap(x + 1, y) == placeable.topPartBlock)
            && !gameWorld.hasForeAt(x, y + 1)) {
            placeable.topPartBlock
        } else {
            placeable.bottomPartBlock
        }

        if (gameWorld.placeToForeground(x, y, slabPart)) {
            mobsController.player.decreaseCurrentItemCount(gameItemsHolder)
        }
    }

    companion object {
        private const val TAG = "PlaceSlabAction"
        const val ACTION_KEY = "place_slab"
    }
}