package ru.fredboy.cavedroid.ux.rendering.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.ux.rendering.IGameRenderer
import ru.fredboy.cavedroid.ux.rendering.WindowsRenderer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.SurvivalInventoryWindow
import javax.inject.Inject
import kotlin.math.atan

@GameScope
class SurvivalWindowRenderer @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val mobController: MobController,
    private val gameWindowsManager: GameWindowsManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.Companion.RENDER_LAYER

    private val survivalWindowTexture get() = requireNotNull(textureRegions[SURVIVAL_WINDOW_KEY])

    private fun setPortraitHeadRotation(portraitX: Float, portraitY: Float) {
        if (applicationContextRepository.isTouch()) {
            return
        }

        val mouseX = Gdx.input.x * (applicationContextRepository.getWidth() / Gdx.graphics.width)
        val mouseY = Gdx.input.y * (applicationContextRepository.getHeight() / Gdx.graphics.height)

        val h = mouseX.toDouble() - portraitX.toDouble()
        val v = mouseY.toDouble() - portraitY.toDouble()

        mobController.player.direction = if (mouseX < portraitX + mobController.player.width / 2) {
            Direction.LEFT
        } else {
            Direction.RIGHT
        }

        mobController.player.headRotation = atan(v / h).toFloat() * MathUtils.radDeg
    }

    private fun drawPlayerPortrait(spriteBatch: SpriteBatch, windowX: Float, windowY: Float, delta: Float) {
        val portraitX = windowX + GameWindowsConfigs.Survival.portraitMarginLeft +
                (GameWindowsConfigs.Survival.portraitWidth / 2 - mobController.player.width / 2)
        val portraitY = windowY + GameWindowsConfigs.Survival.portraitMarginTop +
                (GameWindowsConfigs.Survival.portraitHeight / 2 - mobController.player.height / 2)

        setPortraitHeadRotation(portraitX, portraitY)
        mobController.player.draw(spriteBatch, portraitX, portraitY, delta)
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
            items = mobController.player.inventory.items.asSequence()
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
            items = mobController.player.inventory.items.asSequence()
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
                    it
                }
            }.asIterable(),
            itemsInRow = GameWindowsConfigs.Survival.craftGridSize,
            cellWidth = GameWindowsConfigs.Survival.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Survival.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
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
        private const val SURVIVAL_WINDOW_KEY = "survival"
    }
}