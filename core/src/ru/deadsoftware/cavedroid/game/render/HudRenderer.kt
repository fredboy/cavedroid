package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameInput
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.ControlMode
import ru.deadsoftware.cavedroid.misc.utils.px
import javax.inject.Inject

@GameScope
class HudRenderer @Inject constructor(
    private val gameInput: GameInput,
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
) : IGameRenderer {

    override val renderLayer = RENDER_LAYER

    private val cursorTexture get() = requireNotNull(Assets.textureRegions[CURSOR_KEY])
    private val hotbarTexture get() = requireNotNull(Assets.textureRegions[HOTBAR_KEY])
    private val hotbarSelectorTexture get() = requireNotNull(Assets.textureRegions[HOTBAR_SELECTOR_KEY])
    private val wholeHeartTexture get() = requireNotNull(Assets.textureRegions[WHOLE_HEART_KEY])
    private val halfHeartTexture get() = requireNotNull(Assets.textureRegions[HALF_HEART_KEY])

    private fun drawCursor(spriteBatch: SpriteBatch, viewport: Rectangle) {
        val cursorX = mobsController.player.cursorX
        val cursorY = mobsController.player.cursorY

        if (gameWorld.hasForeAt(cursorX, cursorY) ||
            gameWorld.hasBackAt(cursorX, cursorY) ||
            gameInput.controlMode == ControlMode.CURSOR
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
        val wholeHearts = player.health / 2

        for (i in 0..<wholeHearts) {
            spriteBatch.draw(wholeHeart, x + i * wholeHeart.regionWidth, y)
        }

        if (player.health % 2 == 1) {
            spriteBatch.draw(halfHeartTexture, x + wholeHearts * wholeHeart.regionWidth, y)
        }
    }

    private fun drawHotbarItems(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer,  hotbarX: Float) {
        mobsController.player.inventory.asSequence().take(HotbarConfig.hotbarCells)
            .forEachIndexed { index, item ->
                if (item.item.isNone()) {
                    return@forEachIndexed
                }

                item.draw(
                    spriteBatch = spriteBatch,
                    shapeRenderer = shapeRenderer,
                    x = hotbarX + HotbarConfig.horizontalMargin +
                            index * (HotbarConfig.itemSeparatorWidth + HotbarConfig.itemSlotSpace),
                    y = HotbarConfig.verticalMargin,
                )
            }
    }

    private fun drawHotbarSelector(spriteBatch: SpriteBatch, hotbarX: Float) {
        spriteBatch.draw(
            /* region = */ hotbarSelectorTexture,
            /* x = */ hotbarX - HotbarSelectorConfig.horizontalPadding
                    + mobsController.player.slot * (HotbarConfig.itemSeparatorWidth + HotbarConfig.itemSlotSpace),
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