package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.debug.DebugInfoStringsProvider
import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindRenderer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.common.utils.bl
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindRenderer
class DebugRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val debugInfoStringsProvider: DebugInfoStringsProvider,
    private val getFont: GetFontUseCase,
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

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
            /* x = */ mobController.player.x - (minimapSize.px / 2),
            /* y = */ mobController.player.y - (minimapSize.px / 2),
            /* width = */ minimapSize.px,
            /* height = */ minimapSize.px
        )

        spriteBatch.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.LIGHT_GRAY
        shapeRenderer.rect(minimapX, minimapY, minimapSize, minimapSize)

        forEachBlockInArea(mapArea) { x, y ->
            getMinimapColor(x, y)?.let { color ->
                shapeRenderer.color = color
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
            spriteBatch.drawString(getFont(), str, 0f, index * 10f)
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