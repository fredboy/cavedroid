package ru.fredboy.cavedroid.ux.rendering.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.ux.rendering.IGameRenderer
import ru.fredboy.cavedroid.ux.rendering.WindowsRenderer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.withScissors
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.FurnaceInventoryWindow
import javax.inject.Inject

@GameScope
class FurnaceWindowRenderer @Inject constructor(
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.Companion.RENDER_LAYER

    private val furnaceWindowTexture get() = requireNotNull(textureRegions[FURNACE_WINDOW_KEY])

    private val furnaceProgress by lazy { Sprite(textureRegions["furnace_progress"]) }
    private val furnaceBurn by lazy { Sprite(textureRegions["furnace_burn"]) }
    
    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = furnaceWindowTexture
        
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)
        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Furnace.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Furnace.itemsGridMarginTop,
            items = mobController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Furnace.hotbarCells)
                .take(GameWindowsConfigs.Furnace.itemsInCol * GameWindowsConfigs.Furnace.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Furnace.itemsInRow,
            cellWidth = GameWindowsConfigs.Furnace.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Furnace.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Furnace.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Furnace.hotbarOffsetFromBottom,
            items = mobController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Furnace.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Furnace.hotbarCells,
            cellWidth = GameWindowsConfigs.Furnace.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Furnace.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        window.furnace.fuel.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            x = windowX + GameWindowsConfigs.Furnace.smeltFuelMarginLeft,
            y = windowY + GameWindowsConfigs.Furnace.smeltFuelMarginTop,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
        )

        window.furnace.input.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            x = windowX + GameWindowsConfigs.Furnace.smeltInputMarginLeft,
            y = windowY + GameWindowsConfigs.Furnace.smeltInputMarginTop,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
        )

        window.furnace.result.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            x = windowX + GameWindowsConfigs.Furnace.smeltResultOffsetX,
            y = windowY + GameWindowsConfigs.Furnace.smeltResultOffsetY,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
        )

        if (window.furnace.isActive) {
            val burn = GameWindowsConfigs.Furnace.fuelBurnHeight * window.furnace.burnProgress

            spriteBatch.withScissors(
                x = windowX + GameWindowsConfigs.Furnace.fuelBurnMarginLeft,
                y = windowY + GameWindowsConfigs.Furnace.fuelBurnMarginTop + burn,
                width = furnaceBurn.width,
                height = GameWindowsConfigs.Furnace.fuelBurnHeight,
                viewportWidth = viewport.width,
                viewportHeight = viewport.height,
            ) {
                spriteBatch.drawSprite(
                    sprite = furnaceBurn,
                    x = windowX + GameWindowsConfigs.Furnace.fuelBurnMarginLeft,
                    y = windowY + GameWindowsConfigs.Furnace.fuelBurnMarginTop
                )
            }

            if (window.furnace.canSmelt()) {
                val progress = GameWindowsConfigs.Furnace.progressWidth * window.furnace.smeltProgress

                spriteBatch.withScissors(
                    x = windowX + GameWindowsConfigs.Furnace.progressMarginLeft,
                    y = windowY + GameWindowsConfigs.Furnace.progressMarginTop,
                    width = progress,
                    height = furnaceProgress.height,
                    viewportWidth = viewport.width,
                    viewportHeight = viewport.height,
                ) {
                    spriteBatch.drawSprite(
                        sprite = furnaceProgress,
                        x = windowX + GameWindowsConfigs.Furnace.progressMarginLeft,
                        y = windowY + GameWindowsConfigs.Furnace.progressMarginTop,
                    )
                }
            }
        }

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
        private const val FURNACE_WINDOW_KEY = "furnace"
    }
}