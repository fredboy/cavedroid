package ru.deadsoftware.cavedroid.game.render.windows

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.Mob
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.render.IGameRenderer
import ru.deadsoftware.cavedroid.game.render.WindowsRenderer
import ru.deadsoftware.cavedroid.misc.Assets
import javax.inject.Inject
import kotlin.math.atan

@GameScope
class SurvivalWindowRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
) : AbstractWindowRenderer(), IGameRenderer {

    override val renderLayer get() = WindowsRenderer.RENDER_LAYER

    private val survivalWindowTexture get() = requireNotNull(Assets.textureRegions[SURVIVAL_WINDOW_KEY])

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
        val portraitX = windowX + Config.portraitMarginLeft +
                (Config.portraitWidth / 2 - mobsController.player.width / 2)
        val portraitY = windowY + Config.portraitMarginTop +
                (Config.portraitHeight / 2 - mobsController.player.height / 2)

        setPortraitHeadRotation(portraitX, portraitY)
        mobsController.player.draw(spriteBatch, portraitX, portraitY, delta)
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val survivalWindow = survivalWindowTexture

        val windowX = viewport.width / 2 - survivalWindow.regionWidth / 2
        val windowY = viewport.height / 2 - survivalWindow.regionHeight / 2

        spriteBatch.draw(survivalWindow, windowX, windowY)

        drawPlayerPortrait(spriteBatch, windowX, windowY, delta)

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + Config.itemsGridMarginLeft,
            gridY = windowY + Config.itemsGridMarginTop,
            items = mobsController.player.inventory.asSequence()
                .drop(Config.hotbarCells)
                .take(Config.itemsInCol * Config.itemsInRow)
                .asIterable(),
            itemsInRow = Config.itemsInRow,
            cellWidth = Config.itemsGridColWidth,
            cellHeight = Config.itemsGridRowHeight,
        )

        drawItemsGrid(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            gridX = windowX + Config.itemsGridMarginLeft,
            gridY = windowY + survivalWindow.regionHeight - Config.hotbarOffsetFromBottom,
            items = mobsController.player.inventory.asSequence()
                .take(Config.hotbarCells)
                .asIterable(),
            itemsInRow = Config.hotbarCells,
            cellWidth = Config.itemsGridColWidth,
            cellHeight = Config.itemsGridRowHeight,
        )
    }

    companion object {
        private const val SURVIVAL_WINDOW_KEY = "survival"

        private data object Config {
            const val itemsGridMarginLeft = 8f
            const val itemsGridMarginTop = 84f

            const val itemsGridRowHeight = 18f
            const val itemsGridColWidth = 18f

            const val itemsInRow = 8
            const val itemsInCol = 5

            const val hotbarOffsetFromBottom = 24f
            const val hotbarCells = 9

            const val portraitMarginLeft = 24f
            const val portraitMarginTop = 8f
            const val portraitWidth = 48f
            const val portraitHeight = 68f
        }
    }
}