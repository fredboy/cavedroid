package ru.fredboy.cavedroid.gameplay.controls.action

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.IPlaceBlockAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.PlaceBlockItemToBackgroundAction
import ru.fredboy.cavedroid.gameplay.controls.action.placeblock.PlaceBlockItemToForegroundAction

private const val TAG = "PlaceBlockActionUtils"
private val logger = co.touchlab.kermit.Logger.withTag(TAG)

fun Map<String, IPlaceBlockAction>.placeToForegroundAction(item: Item.Placeable, x: Int, y: Int): Boolean {
    return get(PlaceBlockItemToForegroundAction.ACTION_KEY)?.place(item, x, y)
        ?: run {
            logger.w { "action place_foreground_block not found" }
            false
        }
}

fun Map<String, IPlaceBlockAction>.placeToBackgroundAction(item: Item.Placeable, x: Int, y: Int): Boolean {
    return get(PlaceBlockItemToBackgroundAction.ACTION_KEY)?.place(item, x, y)
        ?: run {
            logger.w { "action place_background_block not found" }
            false
        }
}
