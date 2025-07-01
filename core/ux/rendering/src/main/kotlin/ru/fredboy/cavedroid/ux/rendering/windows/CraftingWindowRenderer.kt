package ru.fredboy.cavedroid.ux.rendering.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.CraftingInventoryWindow
import ru.fredboy.cavedroid.ux.rendering.IGameRenderer
import ru.fredboy.cavedroid.ux.rendering.WindowsRenderer
import javax.inject.Inject

@GameScope
class CraftingWindowRenderer @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(),
    IGameRenderer {

    override val renderLayer get() = WindowsRenderer.Companion.RENDER_LAYER

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
            items = mobController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Crafting.hotbarCells)
                .take(GameWindowsConfigs.Crafting.itemsInCol * GameWindowsConfigs.Crafting.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.itemsInRow,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Crafting.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Crafting.hotbarOffsetFromBottom,
            items = mobController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Crafting.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Crafting.hotbarCells,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Crafting.craftOffsetX,
            gridY = windowY + GameWindowsConfigs.Crafting.craftOffsetY,
            items = window.craftingItems,
            itemsInRow = GameWindowsConfigs.Crafting.craftGridSize,
            cellWidth = GameWindowsConfigs.Crafting.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Crafting.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        window.craftResult.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            x = windowX + GameWindowsConfigs.Crafting.craftResultOffsetX,
            y = windowY + GameWindowsConfigs.Crafting.craftResultOffsetY,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
        )

        window.selectedItem?.drawSelected(
            spriteBatch = spriteBatch,
            font = getFont(),
            x = Gdx.input.x * (viewport.width / Gdx.graphics.width),
            y = Gdx.input.y * (viewport.height / Gdx.graphics.height),
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
        )
    }

    companion object {
        private const val CRAFTING_WINDOW_KEY = "crafting_table"
    }
}
