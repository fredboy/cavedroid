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
import ru.deadsoftware.cavedroid.game.ui.windows.inventory.SurvivalInventoryWindow
import ru.deadsoftware.cavedroid.misc.Assets
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import javax.inject.Inject
import kotlin.math.atan

@GameScope
class SurvivalWindowRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWindowsManager: GameWindowsManager,
    private val gameItemsHolder: GameItemsHolder,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val survivalWindowTexture get() = requireNotNull(textureRegions[SURVIVAL_WINDOW_KEY])

    private fun setPortraitHeadRotation(portraitX: Float, portraitY: Float) {
        if (mainConfig.isTouch) {
            return
        }

        val mouseX = Gdx.input.x * (mainConfig.width / Gdx.graphics.width)
        val mouseY = Gdx.input.y * (mainConfig.height / Gdx.graphics.height)

        val h = mouseX.toDouble() - portraitX.toDouble()
        val v = mouseY.toDouble() - portraitY.toDouble()

        mobsController.player.setDir(
            if (mouseX < portraitX + mobsController.player.width / 2)
                Mob.Direction.LEFT
            else
                Mob.Direction.RIGHT
        )

        mobsController.player.headRotation = atan(v / h).toFloat() * MathUtils.radDeg
    }

    private fun drawPlayerPortrait(spriteBatch: SpriteBatch, windowX: Float, windowY: Float, delta: Float) {
        val portraitX = windowX + GameWindowsConfigs.Survival.portraitMarginLeft +
                (GameWindowsConfigs.Survival.portraitWidth / 2 - mobsController.player.width / 2)
        val portraitY = windowY + GameWindowsConfigs.Survival.portraitMarginTop +
                (GameWindowsConfigs.Survival.portraitHeight / 2 - mobsController.player.height / 2)

        setPortraitHeadRotation(portraitX, portraitY)
        mobsController.player.draw(spriteBatch, portraitX, portraitY, delta)
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val windowTexture = survivalWindowTexture
        val window = gameWindowsManager.currentWindow as SurvivalInventoryWindow

        val windowX = viewport.width / 2 - windowTexture.regionWidth / 2
        val windowY = viewport.height / 2 - windowTexture.regionHeight / 2

        spriteBatch.draw(windowTexture, windowX, windowY)

        drawPlayerPortrait(spriteBatch, windowX, windowY, delta)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Survival.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Survival.itemsGridMarginTop,
            items = mobsController.player.inventory.items.asSequence()
                .drop(GameWindowsConfigs.Survival.hotbarCells)
                .take(GameWindowsConfigs.Survival.itemsInCol * GameWindowsConfigs.Survival.itemsInRow)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Survival.itemsInRow,
            cellWidth = GameWindowsConfigs.Survival.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Survival.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Survival.itemsGridMarginLeft,
            gridY = windowY + windowTexture.regionHeight - GameWindowsConfigs.Survival.hotbarOffsetFromBottom,
            items = mobsController.player.inventory.items.asSequence()
                .take(GameWindowsConfigs.Survival.hotbarCells)
                .asIterable(),
            itemsInRow = GameWindowsConfigs.Survival.hotbarCells,
            cellWidth = GameWindowsConfigs.Survival.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Survival.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
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
                    it ?: gameItemsHolder.fallbackItem.toInventoryItem()
                }
            }.asIterable(),
            itemsInRow = GameWindowsConfigs.Survival.craftGridSize,
            cellWidth = GameWindowsConfigs.Survival.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Survival.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        window.craftResult?.draw(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            x = windowX + GameWindowsConfigs.Survival.craftResultOffsetX,
            y = windowY + GameWindowsConfigs.Survival.craftResultOffsetY,
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
        private const val SURVIVAL_WINDOW_KEY = "survival"
    }
}