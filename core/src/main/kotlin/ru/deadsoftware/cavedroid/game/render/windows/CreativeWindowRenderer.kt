package ru.deadsoftware.cavedroid.game.render.windows

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.deadsoftware.cavedroid.game.render.WindowsRenderer
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsConfigs
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import javax.inject.Inject
import kotlin.math.min

@GameScope
class CreativeWindowRenderer @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val gameItemsHolder: GameItemsHolder,
    private val mobsController: MobsController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val creativeWindowTexture get() = requireNotNull(textureRegions[CREATIVE_WINDOW_KEY])
    private val scrollIndicatorTexture get() = requireNotNull(textureRegions[SCROLL_INDICATOR_KEY])


    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val creativeWindow = creativeWindowTexture

        val windowX = viewport.width / 2 - creativeWindow.regionWidth / 2
        val windowY = viewport.height / 2 - creativeWindow.regionHeight / 2
        val oneScrollAmount = GameWindowsConfigs.Creative.scrollIndicatorFullHeight / gameItemsHolder.getMaxCreativeScrollAmount()

        spriteBatch.draw(creativeWindow, windowX, windowY)
        spriteBatch.draw(
            /* region = */ scrollIndicatorTexture,
            /* x = */ windowX + GameWindowsConfigs.Creative.scrollIndicatorMarginLeft,
            /* y = */ windowY + GameWindowsConfigs.Creative.scrollIndicatorMarginTop
                    + (gameWindowsManager.creativeScrollAmount * oneScrollAmount)
        )

        val allItems = gameItemsHolder.getAllItems()
        val startIndex = gameWindowsManager.creativeScrollAmount * GameWindowsConfigs.Creative.itemsInRow
        val endIndex = min(startIndex + GameWindowsConfigs.Creative.itemsOnPage, allItems.size)
        val items = sequence {
            for (i in startIndex..<endIndex) {
                yield(allItems.elementAt(i))
            }
        }

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Creative.itemsGridMarginLeft,
            gridY = windowY + GameWindowsConfigs.Creative.itemsGridMarginTop,
            items = items.asIterable(),
            itemsInRow = GameWindowsConfigs.Creative.itemsInRow,
            cellWidth = GameWindowsConfigs.Creative.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Creative.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Creative.itemsGridMarginLeft,
            gridY = windowY + creativeWindow.regionHeight - GameWindowsConfigs.Creative.playerInventoryOffsetFromBottom,
            items = mobsController.player.inventory.items.asSequence().take(GameWindowsConfigs.Creative.invItems).asIterable(),
            itemsInRow = GameWindowsConfigs.Creative.invItems,
            cellWidth = GameWindowsConfigs.Creative.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Creative.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight
        )
    }

    companion object {
        private const val CREATIVE_WINDOW_KEY = "creative"
        private const val SCROLL_INDICATOR_KEY = "handle"
    }
}