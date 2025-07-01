package ru.fredboy.cavedroid.ux.rendering.windows

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.window.GameWindowsConfigs
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryWindow
import ru.fredboy.cavedroid.ux.rendering.IGameRenderer
import ru.fredboy.cavedroid.ux.rendering.WindowsRenderer
import javax.inject.Inject
import kotlin.math.min

@GameScope
class CreativeWindowRenderer @Inject constructor(
    private val gameWindowsManager: GameWindowsManager,
    private val itemsRepository: ItemsRepository,
    private val mobController: MobController,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : AbstractWindowRenderer(),
    IGameRenderer {

    override val renderLayer get() = WindowsRenderer.Companion.RENDER_LAYER

    private val creativeWindowTexture get() = requireNotNull(textureRegions[CREATIVE_WINDOW_KEY])
    private val scrollIndicatorTexture get() = requireNotNull(textureRegions[SCROLL_INDICATOR_KEY])

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val creativeWindow = creativeWindowTexture

        val windowX = viewport.width / 2 - creativeWindow.regionWidth / 2
        val windowY = viewport.height / 2 - creativeWindow.regionHeight / 2
        val oneScrollAmount = GameWindowsConfigs.Creative.scrollIndicatorFullHeight /
            (gameWindowsManager.currentWindow as CreativeInventoryWindow).getMaxScroll(itemsRepository)

        spriteBatch.draw(creativeWindow, windowX, windowY)
        spriteBatch.draw(
            /* region = */ scrollIndicatorTexture,
            /* x = */ windowX + GameWindowsConfigs.Creative.scrollIndicatorMarginLeft,
            /* y = */ windowY + GameWindowsConfigs.Creative.scrollIndicatorMarginTop +
                (gameWindowsManager.creativeScrollAmount * oneScrollAmount),
        )

        val allItems = itemsRepository.getAllItems()
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
            getStringHeight = getStringHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = getFont(),
            gridX = windowX + GameWindowsConfigs.Creative.itemsGridMarginLeft,
            gridY = windowY + creativeWindow.regionHeight - GameWindowsConfigs.Creative.playerInventoryOffsetFromBottom,
            items = mobController.player.inventory.items.asSequence().take(GameWindowsConfigs.Creative.invItems).asIterable(),
            itemsInRow = GameWindowsConfigs.Creative.invItems,
            cellWidth = GameWindowsConfigs.Creative.itemsGridColWidth,
            cellHeight = GameWindowsConfigs.Creative.itemsGridRowHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )
    }

    companion object {
        private const val CREATIVE_WINDOW_KEY = "creative"
        private const val SCROLL_INDICATOR_KEY = "handle"
    }
}
