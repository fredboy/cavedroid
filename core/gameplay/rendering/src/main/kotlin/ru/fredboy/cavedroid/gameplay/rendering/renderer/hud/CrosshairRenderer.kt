package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.ifTrue
import ru.fredboy.cavedroid.common.utils.invertColor
import ru.fredboy.cavedroid.common.utils.pixels
import ru.fredboy.cavedroid.domain.assets.repository.MobTextureAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class CrosshairRenderer @Inject constructor(
    private val mobController: MobController,
    private val mobAssetsRepository: MobTextureAssetsRepository,
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameContextRepository: GameContextRepository,
) : IHudRenderer {

    override val renderLayer: Int
        get() = RENDER_LAYER

    private fun getTintColor(x: Float, y: Float, viewport: Rectangle): Color {
        val pixmap = Pixmap.createFromFrameBuffer(
            /* x = */ (x * (Gdx.graphics.width / viewport.width)).toInt(),
            /* y = */ Gdx.graphics.height - (y * (Gdx.graphics.height / viewport.height)).toInt(),
            /* w = */ 1,
            /* h = */ 1,
        )

        return Color(invertColor(pixmap.getPixel(0, 0))).also {
            pixmap.dispose()
        }
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        delta: Float,
    ) {
        if (applicationContextRepository.isTouch() || gameContextRepository.shouldShowInfo()) {
            mobAssetsRepository.getCrosshairSprite().let { crosshairSprite ->
                val x = (mobController.player.cursorX - gameContextRepository.getCameraContext().visibleWorld.x).pixels
                val y = (mobController.player.cursorY - gameContextRepository.getCameraContext().visibleWorld.y).pixels

                val tintColor = ifTrue(Gdx.graphics.framesPerSecond > 100) {
                    getTintColor(x, y, viewport)
                }

                spriteBatch.drawSprite(
                    sprite = crosshairSprite,
                    x = x - crosshairSprite.regionWidth / 2f,
                    y = y - crosshairSprite.regionHeight / 2f,
                    width = crosshairSprite.regionWidth.toFloat(),
                    height = crosshairSprite.regionHeight.toFloat(),
                    tint = tintColor,
                )
            }
        }
    }

    companion object {
        private const val RENDER_LAYER = 100475
    }
}
