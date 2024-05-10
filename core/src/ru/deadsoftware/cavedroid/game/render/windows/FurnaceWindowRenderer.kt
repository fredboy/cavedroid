package ru.deadsoftware.cavedroid.game.render.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.TimeUtils
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.objects.furnace.Furnace
import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.deadsoftware.cavedroid.game.render.WindowsRenderer
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.FurnaceInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.drawSprite
import ru.deadsoftware.cavedroid.misc.utils.withScissors
import javax.inject.Inject

@GameScope
class FurnaceWindowRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameItemsHolder: GameItemsHolder,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val furnaceWindowTexture get() = requireNotNull(Assets.textureRegions[FURNACE_WINDOW_KEY])

    
    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = furnaceWindowTexture
        
        val window = gameWindowsManager.currentWindow as FurnaceInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)
        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Furnace.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Furnace.itemsGridMarginTop,
            items = mobsController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Furnace.hotbarCells)
                .take(GameWindowsConfigs.Furnace.itemsInCol * GameWindowsConfigs.Furnace.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Furnace.itemsInRow,
            cellWidth = GameWindowsConfigs.Furnace.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Furnace.itemsGridRowHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Furnace.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Furnace.hotbarOffsetFromBottom,
            items = mobsController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Furnace.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Furnace.hotbarCells,
            cellWidth = GameWindowsConfigs.Furnace.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Furnace.itemsGridRowHeight,
        )

        window.furnace.fuel?.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            x = windowX + GameWindowsConfigs.Furnace.smeltFuelMarginLeft,
            y = windowY + GameWindowsConfigs.Furnace.smeltFuelMarginTop
        )

        window.furnace.input?.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            x = windowX + GameWindowsConfigs.Furnace.smeltInputMarginLeft,
            y = windowY + GameWindowsConfigs.Furnace.smeltInputMarginTop
        )

        window.furnace.result?.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            x = windowX + GameWindowsConfigs.Furnace.smeltResultOffsetX,
            y = windowY + GameWindowsConfigs.Furnace.smeltResultOffsetY
        )

        if (window.furnace.isActive) {
            val burn = GameWindowsConfigs.Furnace.fuelBurnHeight * window.furnace.burnProgress

            spriteBatch.withScissors(
                mainConfig = mainConfig,
                x = windowX + GameWindowsConfigs.Furnace.fuelBurnMarginLeft,
                y = windowY + GameWindowsConfigs.Furnace.fuelBurnMarginTop + burn,
                width = Assets.furnaceBurn.width,
                height = GameWindowsConfigs.Furnace.fuelBurnHeight,
            ) {
                spriteBatch.drawSprite(
                    sprite = Assets.furnaceBurn,
                    x = windowX + GameWindowsConfigs.Furnace.fuelBurnMarginLeft,
                    y = windowY + GameWindowsConfigs.Furnace.fuelBurnMarginTop
                )
            }

            if (window.furnace.canSmelt()) {
                val progress = GameWindowsConfigs.Furnace.progressWidth * window.furnace.smeltProgress

                spriteBatch.withScissors(
                    mainConfig = mainConfig,
                    x = windowX + GameWindowsConfigs.Furnace.progressMarginLeft,
                    y = windowY + GameWindowsConfigs.Furnace.progressMarginTop,
                    width = progress,
                    height = Assets.furnaceProgress.height
                ) {
                    spriteBatch.drawSprite(
                        sprite = Assets.furnaceProgress,
                        x = windowX + GameWindowsConfigs.Furnace.progressMarginLeft,
                        y = windowY + GameWindowsConfigs.Furnace.progressMarginTop,
                    )
                }
            }
        }

        window.selectedItem?.drawSelected(
            spriteBatch = spriteBatch,
            x = Gdx.input.x * (viewport.width / Gdx.graphics.width),
            y = Gdx.input.y * (viewport.height / Gdx.graphics.height)
        )
    }

    companion object {
        private const val FURNACE_WINDOW_KEY = "furnace"
    }
}