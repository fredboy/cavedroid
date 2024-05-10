package ru.deadsoftware.cavedroid.game.render.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.Mob
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.deadsoftware.cavedroid.game.render.WindowsRenderer
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.ChestInventoryWindow
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.SurvivalInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject
import kotlin.math.atan

@GameScope
class ChestWindowRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameItemsHolder: GameItemsHolder,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val chestWindowTexture get() = requireNotNull(Assets.textureRegions[CHEST_WINDOW_KEY])
    
    
    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = chestWindowTexture
        val window = gameWindowsManager.currentWindow as ChestInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Chest.contentsMarginLeft,
            gridY = windowY + GameWindowsConfigs.Chest.contentsMarginTop,
            items = window.chest.items,
            itemsInRow = GameWindowsConfigs.Chest.itemsInRow,
            cellWidth = GameWindowsConfigs.Chest.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Chest.itemsGridRowHeight,
        )
        
        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Chest.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Chest.itemsGridMarginTop,
            items = mobsController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Chest.hotbarCells)
                .take(GameWindowsConfigs.Chest.itemsInCol * GameWindowsConfigs.Chest.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Chest.itemsInRow,
            cellWidth = GameWindowsConfigs.Chest.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Chest.itemsGridRowHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + GameWindowsConfigs.Chest.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Chest.hotbarOffsetFromBottom,
            items = mobsController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Chest.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Chest.hotbarCells,
            cellWidth = GameWindowsConfigs.Chest.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Chest.itemsGridRowHeight,
        )

        window.selectedItem?.drawSelected(
            spriteBatch = spriteBatch,
            x = Gdx.input.x * (viewport.width / Gdx.graphics.width),
            y = Gdx.input.y * (viewport.height / Gdx.graphics.height)
        )
    }

    companion object {
        private const val CHEST_WINDOW_KEY = "chest"
    }
}