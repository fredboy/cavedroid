package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.debug.DebugInfoStringsProvider
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.bl
import ru.deadsoftware.cavedroid.misc.utils.forEachBlockInArea
import ru.deadsoftware.cavedroid.misc.utils.px
import javax.inject.Inject

@GameScope
class DebugRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
    private val debugInfoStringsProvider: DebugInfoStringsProvider,
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

    private fun SpriteBatch.drawString(str: String, x: Float, y: Float) {
        Assets.minecraftFont.draw(this, str, x, y)
    }

    private fun getMinimapColor(x: Int, y: Int): Color? {
        val foregroundBlock = gameWorld.getForeMap(x, y)

        return if (!foregroundBlock.isNone()) {
            when (foregroundBlock) {
                is Block.Water -> Color.BLUE
                is Block.Lava -> Color.RED
                else -> Color.BLACK
            }
        } else if (gameWorld.hasBackAt(x, y)) {
            Color.DARK_GRAY
        } else {
            null
        }
    }

    private fun drawMinimap(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        minimapX: Float,
        minimapY: Float,
        minimapSize: Float
    ) {
        val mapArea = Rectangle(
            /* x = */ mobsController.player.x - (minimapSize.px / 2),
            /* y = */ mobsController.player.y - (minimapSize.px / 2),
            /* width = */ minimapSize.px,
            /* height = */ minimapSize.px
        )

        spriteBatch.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.LIGHT_GRAY
        shapeRenderer.rect(minimapX, minimapY, minimapSize, minimapSize)

        forEachBlockInArea(mapArea) { x, y ->
            getMinimapColor(x, y)?.let { color ->
                shapeRenderer.setColor(color)
                shapeRenderer.rect(
                    /* x = */ minimapX + (x - mapArea.x.bl),
                    /* y = */ minimapY + (y - mapArea.y.bl),
                    /* width = */ 1f,
                    /* height = */ 1f
                )
            }
        }

        shapeRenderer.color = Color.OLIVE
        shapeRenderer.rect(minimapX + minimapSize / 2, minimapY + minimapSize / 2, 1f, 2f)
        shapeRenderer.end()
        spriteBatch.begin()
    }

    private fun drawDebugInfo(spriteBatch: SpriteBatch) {
        debugInfoStringsProvider.getDebugStrings().forEachIndexed { index, str ->
            spriteBatch.drawString(str, 0f, index * 10f)
        }
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        if (mainConfig.isShowInfo) {
            drawDebugInfo(spriteBatch)
        }

        if (mainConfig.isShowMap) {
            drawMinimap(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                minimapX = viewport.width - MinimapConfig.margin - MinimapConfig.size,
                minimapY = MinimapConfig.margin,
                minimapSize = MinimapConfig.size
            )
        }

    }

    companion object {
        private const val RENDER_LAYER = Int.MAX_VALUE

        private data object MinimapConfig {
            const val margin = 24f
            const val size = 64f
        }
    }
}