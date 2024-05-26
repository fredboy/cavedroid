package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.player.Player
import ru.deadsoftware.cavedroid.game.mobs.player.Player.ControlMode
import ru.deadsoftware.cavedroid.game.ui.TooltipManager
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindRenderer
import ru.deadsoftware.cavedroid.misc.utils.drawString
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.utils.px
import javax.inject.Inject

@GameScope
@BindRenderer
class HudRenderer @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
    private val tooltipManager: TooltipManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : IGameRenderer {

    override val renderLayer = RENDER_LAYER

    private val cursorTexture get() = requireNotNull(textureRegions[CURSOR_KEY])
    private val hotbarTexture get() = requireNotNull(textureRegions[HOTBAR_KEY])
    private val hotbarSelectorTexture get() = requireNotNull(textureRegions[HOTBAR_SELECTOR_KEY])
    private val wholeHeartTexture get() = requireNotNull(textureRegions[WHOLE_HEART_KEY])
    private val emptyHeartTexture get() = requireNotNull(textureRegions[EMPTY_HEART_KEY])
    private val halfHeartTexture get() = requireNotNull(textureRegions[HALF_HEART_KEY])

    private fun drawCursor(spriteBatch: SpriteBatch, viewport: Rectangle) {
        val cursorX = mobsController.player.cursorX
        val cursorY = mobsController.player.cursorY

        if (gameWorld.hasForeAt(cursorX, cursorY) ||
            gameWorld.hasBackAt(cursorX, cursorY) ||
            mobsController.player.controlMode == ControlMode.CURSOR
        ) {
            spriteBatch.draw(cursorTexture, cursorX.px - viewport.x, cursorY.px - viewport.y)
        }
    }

    private fun drawHealth(spriteBatch: SpriteBatch, x: Float, y: Float) {
        val player = mobsController.player

        if (player.gameMode == 1) {
            return
        }

        val wholeHeart = wholeHeartTexture
        val halfHeart = halfHeartTexture
        val emptyHeart = emptyHeartTexture

        val totalHearts = Player.MAX_HEALTH / 2
        val wholeHearts = player.health / 2

        for (i in 0..< totalHearts) {
            if (i < wholeHearts) {
                spriteBatch.draw(wholeHeart, x + i * wholeHeart.regionWidth, y)
            } else if (i == wholeHearts && player.health % 2 == 1) {
                spriteBatch.draw(halfHeart, x + i * wholeHeart.regionWidth, y)
            } else {
                spriteBatch.draw(emptyHeart, x + i * wholeHeart.regionWidth, y)
            }
        }




    }

    private fun drawHotbarItems(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer,  hotbarX: Float) {
        mobsController.player.inventory.items.asSequence().take(HotbarConfig.hotbarCells)
            .forEachIndexed { index, item ->
                if (item.item.isNone()) {
                    return@forEachIndexed
                }

                item.draw(
                    spriteBatch = spriteBatch,
                    shapeRenderer = shapeRenderer,
                    font = getFont(),
                    x = hotbarX + HotbarConfig.horizontalMargin +
                            index * (HotbarConfig.itemSeparatorWidth + HotbarConfig.itemSlotSpace),
                    y = HotbarConfig.verticalMargin,
                    getStringWidth = getStringWidth,
                    getStringHeight = getStringHeight,
                )
            }
    }

    private fun drawHotbarSelector(spriteBatch: SpriteBatch, hotbarX: Float) {
        spriteBatch.draw(
            /* region = */ hotbarSelectorTexture,
            /* x = */ hotbarX - HotbarSelectorConfig.horizontalPadding
                    + mobsController.player.inventory.activeSlot * (HotbarConfig.itemSeparatorWidth + HotbarConfig.itemSlotSpace),
            /* y = */ -HotbarSelectorConfig.verticalPadding
        )
    }

    private fun drawHotbar(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle) {
        val hotbar = hotbarTexture
        val hotbarX = viewport.width / 2 - hotbar.regionWidth / 2

        spriteBatch.draw(hotbar, hotbarX, 0f)
        drawHealth(spriteBatch, hotbarX, hotbarTexture.regionHeight.toFloat())
        drawHotbarSelector(spriteBatch, hotbarX)
        drawHotbarItems(spriteBatch, shapeRenderer, hotbarX)

        val tooltip = tooltipManager.currentHotbarTooltip
        if (tooltip.isNotBlank()) {
            spriteBatch.drawString(
                font = getFont(),
                str = tooltip,
                x = viewport.width / 2 - getStringWidth(tooltip) / 2,
                y = hotbarTexture.regionHeight.toFloat()
            )
        }
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        drawCursor(spriteBatch, viewport)
        drawHotbar(spriteBatch, shapeRenderer, viewport)
    }

    companion object {
        private const val RENDER_LAYER = 100500

        private const val CURSOR_KEY = "cursor"
        private const val HOTBAR_KEY = "hotbar"
        private const val HOTBAR_SELECTOR_KEY = "hotbar_selector"
        private const val WHOLE_HEART_KEY = "heart_whole"
        private const val HALF_HEART_KEY = "heart_half"
        private const val EMPTY_HEART_KEY = "heart_empty"

        private data object HotbarConfig {
            const val horizontalMargin = 3f
            const val verticalMargin = 3f
            const val itemSeparatorWidth = 4f
            const val itemSlotSpace = 16f
            const val hotbarCells = 9
        }

        private data object HotbarSelectorConfig {
            const val horizontalPadding = 1f
            const val verticalPadding = 1f
        }
    }
}