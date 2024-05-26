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
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.CraftingInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import javax.inject.Inject

@GameScope
class CraftingWindowRenderer @Inject constructor(
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameItemsHolder: GameItemsHolder,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val craftingWindowTexture get() = requireNotNull(textureRegions[CRAFTING_WINDOW_KEY])

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = craftingWindowTexture
        val window = gameWindowsManager.currentWindow as CraftingInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Crafting.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Crafting.itemsGridMarginTop,
            items = mobsController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Crafting.hotbarCells)
                .take(GameWindowsConfigs.Crafting.itemsInCol * GameWindowsConfigs.Crafting.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.itemsInRow,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Crafting.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Crafting.hotbarOffsetFromBottom,
            items = mobsController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Crafting.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.hotbarCells,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Crafting.craftOffsetX,
            gridY = windowY + GameWindowsConfigs.Crafting.craftOffsetY,
            items = window.craftingItems.asSequence().map {  it ?: gameItemsHolder.fallbackItem.toInventoryItem()}.asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.craftGridSize,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        window.craftResult?.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            x = windowX + GameWindowsConfigs.Crafting.craftResultOffsetX,
            y = windowY + GameWindowsConfigs.Crafting.craftResultOffsetY,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        window.selectedItem?.drawSelected(
            spriteBatch = spriteBatch,
            font = getFont(),
            x = Gdx.input.x * (viewport.width / Gdx.graphics.width),
            y = Gdx.input.y * (viewport.height / Gdx.graphics.height),
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )
    }

    companion object {
        private const val CRAFTING_WINDOW_KEY = "crafting_table"
    }
}