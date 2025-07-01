package ru.fredboy.cavedroid.ux.controls.action

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.ux.controls.action.placeblock.IPlaceBlockAction
import ru.fredboy.cavedroid.ux.controls.action.placeblock.PlaceBlockItemToBackgroundAction
import ru.fredboy.cavedroid.ux.controls.action.placeblock.PlaceBlockItemToForegroundAction

private const val TAG = "PlaceBlockActionUtils"

fun Map<String, IPlaceBlockAction>.placeToForegroundAction(item: Item.Placeable, x: Int, y: Int) {
    get(PlaceBlockItemToForegroundAction.ACTION_KEY)?.place(item, x, y)
        ?: Gdx.app.error(TAG, "action place_foreground_block not found")
}

fun Map<String, IPlaceBlockAction>.placeToBackgroundAction(item: Item.Placeable, x: Int, y: Int) {
    get(PlaceBlockItemToBackgroundAction.ACTION_KEY)?.place(item, x, y)
        ?: Gdx.app.error(TAG, "action place_background_block not found")
}
