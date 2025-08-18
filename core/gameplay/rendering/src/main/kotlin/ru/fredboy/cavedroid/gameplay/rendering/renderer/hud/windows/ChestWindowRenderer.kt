package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows

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
import ru.fredboy.cavedroid.game.window.inventory.ChestInventoryWindow
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.WindowsRenderer
import javax.inject.Inject

@GameScope
class ChestWindowRenderer @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(),
    IHudRenderer {

    override val renderLayer get() = WindowsRenderer.Companion.RENDER_LAYER

    private val chestWindowTexture get() = requireNotNull(textureRegions[CHEST_WINDOW_KEY])

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = chestWindowTexture
        val window = gameWindowsManager.currentWindow as ChestInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Chest.contentsMarginLeft,
            gridY = windowY + GameWindowsConfigs.Chest.contentsMarginTop,
            items = window.chest.items,
            itemsInRow = GameWindowsConfigs.Chest.itemsInRow,
            cellWidth = GameWindowsConfigs.Chest.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Chest.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Chest.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Chest.itemsGridMarginTop,
            items = mobController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Chest.hotbarCells)
                .take(GameWindowsConfigs.Chest.itemsInCol * GameWindowsConfigs.Chest.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Chest.itemsInRow,
            cellWidth = GameWindowsConfigs.Chest.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Chest.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Chest.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Chest.hotbarOffsetFromBottom,
            items = mobController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Chest.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Chest.hotbarCells,
            cellWidth = GameWindowsConfigs.Chest.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Chest.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
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
        private const val CHEST_WINDOW_KEY = "chest"
    }
}
