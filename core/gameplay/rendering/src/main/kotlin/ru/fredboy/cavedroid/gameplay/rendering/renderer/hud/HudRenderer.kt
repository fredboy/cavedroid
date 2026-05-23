package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TooltipManager
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
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
    private val wholeBubbleTexture get() = requireNotNull(textureRegions[WHOLE_BUBBLE_KEY])
    private val halfBubbleTexture get() = requireNotNull(textureRegions[HALF_BUBBLE_KEY])
    private val wholeFoodTexture get() = requireNotNull(textureRegions[WHOLE_FOOD_KEY])
    private val halfFoodTexture get() = requireNotNull(textureRegions[HALF_FOOD_KEY])
    private val emptyFoodTexture get() = requireNotNull(textureRegions[EMPTY_FOOD_KEY])
    private val wholeShieldTexture get() = requireNotNull(textureRegions[WHOLE_SHIELD_KEY])
    private val emptyShieldTexture get() = requireNotNull(textureRegions[EMPTY_SHIELD_KEY])
    private val halfShieldTexture get() = requireNotNull(textureRegions[HALF_SHIELD_KEY])

    private fun drawHealth(spriteBatch: SpriteBatch, x: Float, y: Float) {
        val player = mobController.player

        if (player.gameMode.isCreative()) {
            return
        }

        val wholeHeart = wholeHeartTexture
        val halfHeart = halfHeartTexture
        val emptyHeart = emptyHeartTexture

        val totalHearts = player.maxHealth / 2
        val wholeHearts = player.health / 2

        val iconWidth = wholeHeart.regionWidth - 1

        for (i in 0..<totalHearts) {
            if (i < wholeHearts) {
                spriteBatch.draw(wholeHeart, x + i * iconWidth, y)
            } else if (i == wholeHearts && player.health % 2 == 1) {
                spriteBatch.draw(halfHeart, x + i * iconWidth, y)
            } else {
                spriteBatch.draw(emptyHeart, x + i * iconWidth, y)
            }
        }
    }

    private fun drawHunger(spriteBatch: SpriteBatch, x: Float, y: Float) {
        val player = mobController.player

        if (player.gameMode.isCreative()) {
            return
        }

        val foodWhole = wholeFoodTexture
        val foodHalf = halfFoodTexture
        val foodEmpty = emptyFoodTexture

        val totalFoods = Player.MAX_FOOD_LEVEL / 2
        val wholeFoods = player.foodLevel / 2

        val iconWidth = foodWhole.regionWidth - 1

        for (i in 0..<totalFoods) {
            val drawX = x - (i + 1) * iconWidth
            if (i < wholeFoods) {
                spriteBatch.draw(foodWhole, drawX, y)
            } else if (i == wholeFoods && player.foodLevel % 2 == 1) {
                spriteBatch.draw(foodHalf, drawX, y)
            } else {
                spriteBatch.draw(foodEmpty, drawX, y)
            }
        }
    }

    private fun drawBreath(spriteBatch: SpriteBatch, x: Float, y: Float) {
        val player = mobController.player

        if (player.gameMode.isCreative()) {
            return
        }

        val iconWidth = wholeBubbleTexture.regionWidth - 1
        val x = x - iconWidth

        if (player.breath < player.params.maxBreath) {
            for (i in 0..<player.breath / 2) {
                spriteBatch.draw(wholeBubbleTexture, x - i *iconWidth, y)
            }
            if (player.breath % 2 == 1) {
                spriteBatch.draw(halfBubbleTexture, x - (player.breath / 2) * iconWidth, y)
            }
        }
    }

    private fun drawArmor(spriteBatch: SpriteBatch, x: Float, y: Float) {
        val player = mobController.player
        val protection = player.wearingArmor.getTotalProtection()

        if (player.gameMode.isCreative() || protection <= 0) {
            return
        }

        val totalShields = player.maxHealth / 2
        val wholeShields = protection / 2
        val iconWidth = wholeShieldTexture.regionWidth - 1

        for (i in 0..<totalShields) {
            if (i < wholeShields) {
                spriteBatch.draw(wholeShieldTexture, x + i * iconWidth, y)
            } else if (i == wholeShields && protection % 2 == 1) {
                spriteBatch.draw(halfShieldTexture, x + i * iconWidth, y)
            } else {
                spriteBatch.draw(emptyShieldTexture, x + i * iconWidth, y)
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
            /* x = */
            hotbarX - HotbarSelectorConfig.horizontalPadding +
                mobController.player.activeSlot * (HotbarConfig.itemSeparatorWidth + HotbarConfig.itemSlotSpace),
            /* y = */ -HotbarSelectorConfig.verticalPadding,
        )
    }

    private fun drawHotbar(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle) {
        val hotbar = hotbarTexture
        val hotbarX = viewport.width / 2 - hotbar.regionWidth / 2

        spriteBatch.draw(hotbar, hotbarX, 0f)
        val firstRowY = hotbarTexture.regionHeight.toFloat()
        val secondRowY = firstRowY + wholeHeartTexture.regionHeight.toFloat() + 1f
        drawHealth(spriteBatch, hotbarX, firstRowY)
        drawHunger(spriteBatch, hotbarX + hotbarTexture.regionWidth, firstRowY)
        drawBreath(spriteBatch, hotbarX + hotbarTexture.regionWidth, secondRowY)
        drawArmor(
            spriteBatch = spriteBatch,
            x = hotbarX,
            y = secondRowY,
        )
        drawHotbarSelector(spriteBatch, hotbarX)
        drawHotbarItems(spriteBatch, shapeRenderer, hotbarX)

        val tooltip = tooltipManager.currentHotbarTooltip
        if (tooltip.isNotBlank()) {
            spriteBatch.drawString(
                font = getFont(),
                str = tooltip,
                x = viewport.width / 2 - getStringWidth(tooltip) / 2,
                y = viewport.height - getStringHeight(tooltip) * 2,
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
        private const val WHOLE_BUBBLE_KEY = "bubble_whole"
        private const val HALF_BUBBLE_KEY = "bubble_half"
        private const val WHOLE_FOOD_KEY = "food_whole"
        private const val HALF_FOOD_KEY = "food_half"
        private const val EMPTY_FOOD_KEY = "food_empty"
        private const val WHOLE_SHIELD_KEY = "shield_whole"
        private const val HALF_SHIELD_KEY = "shield_half"
        private const val EMPTY_SHIELD_KEY = "shield_empty"

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
