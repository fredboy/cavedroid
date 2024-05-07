package ru.deadsoftware.cavedroid.game.render.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.deadsoftware.cavedroid.game.render.WindowsRenderer
import ru.deadsoftware.cavedroid.game.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.windows.inventory.CraftingInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject

@GameScope
class CraftingWindowRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameItemsHolder: GameItemsHolder,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val craftingWindowTexture get() = requireNotNull(Assets.textureRegions[CRAFTING_WINDOW_KEY])

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = craftingWindowTexture
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Crafting.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Crafting.itemsGridMarginTop,
            items = mobsController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Crafting.hotbarCells)
                .take(GameWindowsConfigs.Crafting.itemsInCol * GameWindowsConfigs.Crafting.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.itemsInRow,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Crafting.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Crafting.hotbarOffsetFromBottom,
            items = mobsController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Crafting.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.hotbarCells,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Crafting.craftOffsetX,
            gridY = windowY + GameWindowsConfigs.Crafting.craftOffsetY,
            items = window.craftingItems.asSequence().map {  it ?: gameItemsHolder.fallbackItem.toInventoryItem()}.asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.craftGridSize,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
        )

        window.craftResult?.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            x = windowX + GameWindowsConfigs.Crafting.craftResultOffsetX,
            y = windowY + GameWindowsConfigs.Crafting.craftResultOffsetY
        )

        window.selectedItem?.drawSelected(
            spriteBatch = spriteBatch,
            x = Gdx.input.x * (viewport.width / Gdx.graphics.width),
            y = Gdx.input.y * (viewport.height / Gdx.graphics.height)
        )
    }

    companion object {
        private const val CRAFTING_WINDOW_KEY = "crafting_table"
    }
}