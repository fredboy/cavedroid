package ru.deadsoftware.cavedroid.game.actions

import com.badlogic.gdx.Gdx
import ru.deadsoftware.cavedroid.game.actions.useitem.IUseItemAction
import ru.deadsoftware.cavedroid.game.actions.useitem.PlaceBlockItemToBackgroundAction
import ru.deadsoftware.cavedroid.game.actions.useitem.PlaceBlockItemToForegroundAction
import ru.deadsoftware.cavedroid.game.objects.Item

private const val TAG = "PlaceBlockActionUtils"

fun Map<String, IUseItemAction>.placeToForegroundAction(item: Item, x: Int, y: Int) {
    get(PlaceBlockItemToForegroundAction.ACTION_KEY)?.perform(item, x, y)
        ?: Gdx.app.error(TAG, "action place_foreground_block not found")
}

fun Map<String, IUseItemAction>.placeToBackgroundAction(item: Item, x: Int, y: Int) {
    get(PlaceBlockItemToBackgroundAction.ACTION_KEY)?.perform(item, x, y)
        ?: Gdx.app.error(TAG, "action place_background_block not found")
}
