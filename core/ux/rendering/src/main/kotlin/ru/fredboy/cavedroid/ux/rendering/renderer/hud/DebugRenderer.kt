package ru.fredboy.cavedroid.ux.rendering.renderer.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.rendering.annotation.BindHudRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class DebugRenderer @Inject constructor(
    private val gameContextRepository: GameContextRepository,
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val debugInfoStringsProvider: DebugInfoStringsProvider,
    private val getFont: GetFontUseCase,
) : IHudRenderer {

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
        minimapSize: Float,
    ) {
        val mapArea = Rectangle(
            /* x = */ mobController.player.position.x - (minimapSize / 2),
            /* y = */ mobController.player.position.y - (minimapSize / 2),
            /* width = */ minimapSize,
            /* height = */ minimapSize,
        )

        spriteBatch.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.LIGHT_GRAY
        shapeRenderer.rect(minimapX, minimapY, minimapSize, minimapSize)

        forEachBlockInArea(mapArea) { x, y ->
            getMinimapColor(x, y)?.let { color ->
                shapeRenderer.color = color
                shapeRenderer.rect(
                    /* x = */ minimapX + (x - mapArea.x),
                    /* y = */ minimapY + (y - mapArea.y),
                    /* width = */ 1f,
                    /* height = */ 1f,
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
        if (gameContextRepository.shouldShowInfo()) {
            drawDebugInfo(spriteBatch)
        }

        if (gameContextRepository.shouldShowMap()) {
            drawMinimap(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                minimapX = viewport.width - MinimapConfig.margin - MinimapConfig.size,
                minimapY = MinimapConfig.margin,
                minimapSize = MinimapConfig.size,
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
