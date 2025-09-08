package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.SurvivalInventoryWindow
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.WindowsRenderer
import javax.inject.Inject

@GameScope
class SurvivalWindowRenderer @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(),
    IHudRenderer {

    override val renderLayer get() = WindowsRenderer.Companion.RENDER_LAYER

    private val survivalWindowTexture get() = requireNotNull(textureRegions[SURVIVAL_WINDOW_KEY])
    private val inventoryCharTexture get() = requireNotNull(textureRegions[INVENTORY_CHAR_KEY])

    private fun drawPlayerPortrait(spriteBatch: SpriteBatch, windowX: Float, windowY: Float) {
        val portraitX = windowX + GameWindowsConfigs.Survival.portraitMarginLeft +
            (GameWindowsConfigs.Survival.portraitWidth / 2 - inventoryCharTexture.regionWidth / 2)
        val portraitY = windowY + GameWindowsConfigs.Survival.portraitMarginTop +
            (GameWindowsConfigs.Survival.portraitHeight / 2 - inventoryCharTexture.regionHeight / 2)

        spriteBatch.draw(inventoryCharTexture, portraitX, portraitY)
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = survivalWindowTexture
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)

        drawPlayerPortrait(spriteBatch, windowX, windowY)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Survival.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Survival.itemsGridMarginTop,
            items = mobController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Survival.hotbarCells)
                .take(GameWindowsConfigs.Survival.itemsInCol * GameWindowsConfigs.Survival.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Survival.itemsInRow,
            cellWidth = GameWindowsConfigs.Survival.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Survival.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Survival.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Survival.hotbarOffsetFromBottom,
            items = mobController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Survival.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Survival.hotbarCells,
            cellWidth = GameWindowsConfigs.Survival.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Survival.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Survival.craftOffsetX,
            gridY = windowY + GameWindowsConfigs.Survival.craftOffsetY,
            items = window.craftingItems.asSequence().mapIndexedNotNull { index, it ->
                if (index % 3 > 1 || index / 3 > 1) {
                    null
                } else {
                    it
                }
            }.asIterable(),
            itemsInRow = GameWindowsConfigs.Survival.craftGridSize,
            cellWidth = GameWindowsConfigs.Survival.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Survival.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        window.craftResult.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            x = windowX + GameWindowsConfigs.Survival.craftResultOffsetX,
            y = windowY + GameWindowsConfigs.Survival.craftResultOffsetY,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
        )

        window.selectedItem?.let { selectedItem ->
            selectedItem.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                font = getFont(),
                x = Gdx.input.x * (viewport.width / Gdx.graphics.width) - 10f,
                y = Gdx.input.y * (viewport.height / Gdx.graphics.height) - 10f,
                getStringWidth = getStringWidth::invoke,
                getStringHeight = getStringHeight::invoke,
                width = 20f,
                height = 20f,
            )

            val itemName = selectedItem.item.params.name
            spriteBatch.drawString(
                font = getFont(),
                str = itemName,
                x = viewport.width / 2f - getStringWidth(itemName) / 2f,
                y = windowY + windowTexture.regionHeight + getStringHeight(itemName),
            )
        }
    }

    companion object {
        private const val SURVIVAL_WINDOW_KEY = "survival"
        private const val INVENTORY_CHAR_KEY = "inventory_char"
    }
}
