package ru.fredboy.cavedroid.gameplay.controls.action.placeblock

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindPlaceBlockAction
import javax.inject.Inject

@GameScope
@BindPlaceBlockAction(stringKey = PlaceSlabAction.ACTION_KEY)
class PlaceSlabAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
) : IPlaceBlockAction {

    override fun place(placeable: Item.Placeable, x: Int, y: Int): Boolean {
        if (placeable !is Item.Slab) {
            Gdx.app.debug(TAG, "Place slab action called on ${placeable.params.key} which is not a slab")
            return false
        }

        val slabPart = if ((
                gameWorld.hasForeAt(x, y - 1) ||
                    gameWorld.getForeMap(x - 1, y) == placeable.topPartBlock ||
                    gameWorld.getForeMap(x + 1, y) == placeable.topPartBlock
                ) &&
            !gameWorld.hasForeAt(x, y) &&
            !gameWorld.hasForeAt(x, y + 1) ||
            gameWorld.getForeMap(x, y) == placeable.bottomPartBlock
        ) {
            placeable.topPartBlock
        } else {
            placeable.bottomPartBlock
        }

        return if (gameWorld.placeToForeground(x, y, slabPart)) {
            mobController.player.decreaseCurrentItemCount()
            true
        } else {
            false
        }
    }

    companion object {
        private const val TAG = "PlaceSlabAction"
        const val ACTION_KEY = "place_slab"
    }
}
