package ru.deadsoftware.cavedroid.game.render.windows

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameInput
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.deadsoftware.cavedroid.game.render.WindowsRenderer
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class CreativeWindowRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val gameInput: GameInput,
    private val gameItemsHolder: GameItemsHolder,
    private val mobsController: MobsController,
) : IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val creativeWindowTexture get() = requireNotNull(Assets.textureRegions[CREATIVE_WINDOW_KEY])
    private val scrollIndicatorTexture get() = requireNotNull(Assets.textureRegions[SCROLL_INDICATOR_KEY])

    private fun drawItemsGrid(spriteBatch: SpriteBatch, gridX: Float, gridY: Float) {
        val allItems = gameItemsHolder.getAllItems()
        val startIndex = gameInput.creativeScroll * CreativeWindowConfig.itemsInRow
        val endIndex = startIndex + CreativeWindowConfig.itemsOnPage

        for (i in startIndex ..< endIndex) {
            if (i !in allItems.indices) {
                break
            }
            val item = allItems.elementAt(i)

            if (item.isNone()) {
                continue
            }

            val gridIndex = i - startIndex

            val itemX = gridX + (gridIndex % CreativeWindowConfig.itemsInRow) * CreativeWindowConfig.itemsGridColWidth
            val itemY = gridY + (gridIndex / CreativeWindowConfig.itemsInRow) * CreativeWindowConfig.itemsGridRowHeight

            spriteBatch.draw(item.sprite, itemX, itemY)
        }
    }

    private fun drawPlayerInventory(spriteBatch: SpriteBatch, inventoryX: Float, inventoryY: Float) {
        mobsController.player.inventory.asSequence()
            .map(InventoryItem::item)
            .forEachIndexed { index, item ->
                if (item.isNone()) {
                    return@forEachIndexed
                }

                val itemX = inventoryX + index * CreativeWindowConfig.itemsGridColWidth

                spriteBatch.draw(item.sprite, itemX, inventoryY)
            }
    }

    private fun drawCreative(spriteBatch: SpriteBatch, viewport: Rectangle) {
        val creativeWindow = creativeWindowTexture

        val windowX = viewport.width / 2 - creativeWindow.regionWidth / 2
        val windowY = viewport.height / 2 - creativeWindow.regionHeight / 2
        val oneScrollAmount = CreativeWindowConfig.scrollIndicatorFullHeight / gameItemsHolder.getCreativeScrollAmount()

        spriteBatch.draw(creativeWindow, windowX, windowY)
        spriteBatch.draw(
            /* region = */ scrollIndicatorTexture,
            /* x = */ windowX + CreativeWindowConfig.scrollIndicatorMarginLeft,
            /* y = */ windowY + CreativeWindowConfig.scrollIndicatorMarginTop
                    + (gameInput.creativeScroll * oneScrollAmount)
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            gridX = windowX + CreativeWindowConfig.itemsGridMarginLeft,
            gridY = windowY + CreativeWindowConfig.itemsGridMarginTop
        )

        drawPlayerInventory(
            spriteBatch = spriteBatch,
            inventoryX = windowX + CreativeWindowConfig.itemsGridMarginLeft,
            inventoryY = windowY + creativeWindow.regionHeight - CreativeWindowConfig.playerInventoryOffsetFromBottom
        )
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
            drawCreative(spriteBatch, viewport)
    }

    companion object {
        private const val CREATIVE_WINDOW_KEY = "creative"
        private const val SCROLL_INDICATOR_KEY = "handle"

        private data object CreativeWindowConfig {
            const val scrollIndicatorMarginLeft = 156f
            const val scrollIndicatorMarginTop = 18f
            const val scrollIndicatorFullHeight = 72f

            const val itemsGridMarginLeft = 8f
            const val itemsGridMarginTop = 18f

            const val itemsGridRowHeight = 18f
            const val itemsGridColWidth = 18f

            const val itemsInRow = 8
            const val itemsInCol = 5

            const val playerInventoryOffsetFromBottom = 24f

            val itemsOnPage get() = itemsInCol * itemsInRow
        }
    }
}