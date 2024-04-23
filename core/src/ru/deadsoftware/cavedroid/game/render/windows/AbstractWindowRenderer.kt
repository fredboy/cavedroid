package ru.deadsoftware.cavedroid.game.render.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.misc.utils.drawSprite

abstract class AbstractWindowRenderer {

    protected inline fun <reified T> drawItemsGrid(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        gridX: Float,
        gridY: Float,
        items: Iterable<T>,
        itemsInRow: Int,
        cellWidth: Float,
        cellHeight: Float
    ) {
        if (T::class != Item::class && T::class != InventoryItem::class) {
            Gdx.app.log(_TAG, "Trying to draw items grid of not items")
            return
        }

        items.forEachIndexed { index, element ->
            val item = element as? Item
            val inventoryItem = element as? InventoryItem

            if (item == null && inventoryItem == null) {
                throw IllegalStateException("This should be unreachable")
            }

            if (item?.isNone() == true || inventoryItem?.item?.isNone() == true) {
                return@forEachIndexed
            }

            val itemX = gridX + (index % itemsInRow) * cellWidth
            val itemY = gridY + (index / itemsInRow) * cellHeight

            inventoryItem?.draw(spriteBatch, shapeRenderer, itemX, itemY)
                ?: item?.let { spriteBatch.drawSprite(it.sprite, itemX, itemY) }
        }
    }

    companion object {
        protected const val _TAG = "AbstractWindowRenderer"
    }

}