package ru.deadsoftware.cavedroid.game.actions

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.game.actions.placeblock.IPlaceBlockAction
import ru.deadsoftware.cavedroid.game.actions.updateblock.IUpdateBlockAction
import ru.deadsoftware.cavedroid.game.actions.updateblock.UpdateRequiresBlockAction
import ru.deadsoftware.cavedroid.game.actions.placeblock.PlaceBlockItemToBackgroundAction
import ru.deadsoftware.cavedroid.game.actions.placeblock.PlaceBlockItemToForegroundAction
import ru.deadsoftware.cavedroid.game.model.item.Item

private const val TAG = "PlaceBlockActionUtils"

fun Map<String, IPlaceBlockAction>.placeToForegroundAction(item: Item.Placeable, x: Int, y: Int) {
    get(PlaceBlockItemToForegroundAction.ACTION_KEY)?.place(item, x, y)
        ?: Gdx.app.error(TAG, "action place_foreground_block not found")
}

fun Map<String, IPlaceBlockAction>.placeToBackgroundAction(item: Item.Placeable, x: Int, y: Int) {
    get(PlaceBlockItemToBackgroundAction.ACTION_KEY)?.place(item, x, y)
        ?: Gdx.app.error(TAG, "action place_background_block not found")
}

fun Map<String, IUpdateBlockAction>.getRequiresBlockAction(): IUpdateBlockAction {
    return requireNotNull(get(UpdateRequiresBlockAction.ACTION_KEY)) { "action requires_block not found" }
}
