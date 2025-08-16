package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item

abstract class AbstractWindowRenderer {

    protected inline fun <reified T> drawItemsGrid(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        font: BitmapFont,
        gridX: Float,
        gridY: Float,
        items: Iterable<T>,
        itemsInRow: Int,
        cellWidth: Float,
        cellHeight: Float,
        getStringWidth: GetStringWidthUseCase,
        getStringHeight: GetStringHeightUseCase,
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

            inventoryItem?.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                font = font,
                x = itemX,
                y = itemY,
                getStringWidth = getStringWidth::invoke,
                getStringHeight = getStringHeight::invoke,
            ) ?: item?.let {
                spriteBatch.drawSprite(
                    sprite = it.sprite,
                    x = itemX,
                    y = itemY,
                    width = it.sprite.regionWidth.toFloat(),
                    height = it.sprite.regionHeight.toFloat(),
                )
            }
        }
    }

    companion object {
        protected const val _TAG = "AbstractWindowRenderer"
    }
}
