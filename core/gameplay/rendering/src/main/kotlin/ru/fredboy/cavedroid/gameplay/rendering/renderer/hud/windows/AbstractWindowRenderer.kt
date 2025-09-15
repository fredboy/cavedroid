package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.common.utils.pixels
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.inventory.AbstractInventoryWindowWithCraftGrid

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

    protected fun getRecipeButtonTextureRegion(
        getTextureRegionByNameUseCase: GetTextureRegionByNameUseCase,
        window: AbstractInventoryWindowWithCraftGrid,
    ): TextureRegion? {
        return getTextureRegionByNameUseCase[
            if (window.recipeBookActive) {
                RECIPE_BUTTON_ACTIVE_KEY
            } else {
                RECIPE_BUTTON_INACTIVE_KEY
            },
        ]
    }

    protected fun drawRecipeBook(
        spriteBatch: SpriteBatch,
        getTextureRegionByNameUseCase: GetTextureRegionByNameUseCase,
        itemsRepository: ItemsRepository,
        font: BitmapFont,
        viewport: Rectangle,
        window: AbstractInventoryWindowWithCraftGrid,
        getStringWidth: GetStringWidthUseCase,
    ) = with(GameWindowsConfigs.RecipeBook) {
        val recipeBookTexture = getTextureRegionByNameUseCase[RECIPE_BOOK_KEY] ?: return
        val recipeAvailableTexture = getTextureRegionByNameUseCase[RECIPE_AVAILABLE_KEY] ?: return
        val recipeUnavailableTexture = getTextureRegionByNameUseCase[RECIPE_UNAVAILABLE_KEY] ?: return
        val recipePrevTexture = getTextureRegionByNameUseCase[RECIPE_PREV_KEY] ?: return
        val recipeNextTexture = getTextureRegionByNameUseCase[RECIPE_NEXT_KEY] ?: return

        val bookX = viewport.width / 2 - recipeBookTexture.regionWidth
        val bookY = viewport.height / 2 - recipeBookTexture.regionHeight / 2

        spriteBatch.draw(recipeBookTexture, bookX, bookY)

        val pages = window.getAvailableCraftingRecipes(itemsRepository).count() / pageSize + 1

        window.getVisibleCraftingRecipes(itemsRepository)
            .forEachIndexed { index, (_, result) ->
                val x = index % gridWidth
                val y = index / gridWidth

                val texture = if (index == window.selectedRecipe) {
                    recipeUnavailableTexture
                } else {
                    recipeAvailableTexture
                }

                spriteBatch.draw(texture, bookX + gridX + x * cellSize, bookY + gridY + y * cellSize)

                val drawRect = (result as? Item.Placeable)?.block?.getSpriteRectangle(0, 0)
                    ?: Rectangle(0f, 0f, 1f, 1f)
                spriteBatch.drawSprite(
                    sprite = result.sprite,
                    x = bookX + gridX + x * cellSize + (cellSize / 2f - 8f) + drawRect.x.pixels,
                    y = bookY + gridY + y * cellSize + (cellSize / 2f - 8f) + drawRect.y.pixels,
                    width = drawRect.width.pixels,
                    height = drawRect.height.pixels,
                )
            }

        val pageText = "${window.recipeBookPage + 1} / $pages"
        spriteBatch.drawString(
            font = font,
            str = pageText,
            x = bookX + recipeBookTexture.regionWidth / 2 - getStringWidth(pageText) / 2,
            y = bookY + recipeBookTexture.regionHeight - 24f,
            color = Color.BLACK,
        )

        spriteBatch.draw(
            recipePrevTexture,
            bookX + prevX,
            bookY + prevY,
        )
        spriteBatch.draw(
            recipeNextTexture,
            bookX + nextX,
            bookY + nextY,
        )
    }

    companion object {
        protected const val _TAG = "AbstractWindowRenderer"

        private const val RECIPE_BOOK_KEY = "recipe_book"
        private const val RECIPE_BUTTON_ACTIVE_KEY = "recipe_button_on"
        private const val RECIPE_BUTTON_INACTIVE_KEY = "recipe_button_off"
        private const val RECIPE_AVAILABLE_KEY = "recipe_available"
        private const val RECIPE_UNAVAILABLE_KEY = "recipe_unavailable"

        private const val RECIPE_PREV_KEY = "recipe_prev"
        private const val RECIPE_NEXT_KEY = "recipe_next"
    }
}
