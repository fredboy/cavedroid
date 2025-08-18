package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

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
import ru.fredboy.cavedroid.game.window.TooltipManager
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class HudRenderer @Inject constructor(
    private val mobController: MobController,
    private val tooltipManager: TooltipManager,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val getFont: GetFontUseCase,
) : IHudRenderer {

    override val renderLayer = RENDER_LAYER

    private val hotbarTexture get() = requireNotNull(textureRegions[HOTBAR_KEY])
    private val hotbarSelectorTexture get() = requireNotNull(textureRegions[HOTBAR_SELECTOR_KEY])
    private val wholeHeartTexture get() = requireNotNull(textureRegions[WHOLE_HEART_KEY])
    private val emptyHeartTexture get() = requireNotNull(textureRegions[EMPTY_HEART_KEY])
    private val halfHeartTexture get() = requireNotNull(textureRegions[HALF_HEART_KEY])

    private fun drawHealth(spriteBatch: SpriteBatch, x: Float, y: Float) {
        val player = mobController.player

        if (player.gameMode == 1) {
            return
        }

        val wholeHeart = wholeHeartTexture
        val halfHeart = halfHeartTexture
        val emptyHeart = emptyHeartTexture

        val totalHearts = player.maxHealth / 2
        val wholeHearts = player.health / 2

        for (i in 0..<totalHearts) {
            if (i < wholeHearts) {
                spriteBatch.draw(wholeHeart, x + i * wholeHeart.regionWidth, y)
            } else if (i == wholeHearts && player.health % 2 == 1) {
                spriteBatch.draw(halfHeart, x + i * wholeHeart.regionWidth, y)
            } else {
                spriteBatch.draw(emptyHeart, x + i * wholeHeart.regionWidth, y)
            }
        }
    }

    private fun drawHotbarItems(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, hotbarX: Float) {
        mobController.player.inventory.items.asSequence().take(HotbarConfig.hotbarCells)
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
                    getStringWidth = getStringWidth::invoke,
                    getStringHeight = getStringHeight::invoke,
                )
            }
    }

    private fun drawHotbarSelector(spriteBatch: SpriteBatch, hotbarX: Float) {
        spriteBatch.draw(
            /* region = */ hotbarSelectorTexture,
            /* x = */ hotbarX - HotbarSelectorConfig.horizontalPadding +
                mobController.player.activeSlot * (HotbarConfig.itemSeparatorWidth + HotbarConfig.itemSlotSpace),
            /* y = */ -HotbarSelectorConfig.verticalPadding,
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
                y = hotbarTexture.regionHeight.toFloat(),
            )
        }
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        drawHotbar(spriteBatch, shapeRenderer, viewport)
    }

    companion object {
        private const val RENDER_LAYER = 100500
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
