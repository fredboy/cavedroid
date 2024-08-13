package ru.deadsoftware.cavedroid.game.actions.placeblock

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindPlaceBlockAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindPlaceBlockAction(stringKey = PlaceSlabAction.ACTION_KEY)
class PlaceSlabAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
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
            mobController.player.decreaseCurrentItemCount()
        }
    }

    companion object {
        private const val TAG = "PlaceSlabAction"
        const val ACTION_KEY = "place_slab"
    }
}