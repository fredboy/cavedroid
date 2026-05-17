package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawMultilineCentered
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class OnboardingOverlayRenderer @Inject constructor(
    private val controller: OnboardingController,
    private val getFont: GetFontUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
) : IHudRenderer {

    override val renderLayer: Int = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        controller.tick(delta.coerceAtMost(MAX_TICK_DELTA))
        val state = controller.state ?: return

        val fadeOut = if (state.isLast && !state.awaitingInput) {
            (1f - state.cooldownProgress).coerceIn(0f, 1f)
        } else {
            1f
        }
        val textAlpha = if (state.awaitingInput) {
            1f
        } else {
            (1f - state.cooldownProgress).coerceIn(0f, 1f)
        }

        spriteBatch.end()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0f, 0f, 0f, DIM_ALPHA * fadeOut)
        shapeRenderer.rect(0f, 0f, viewport.width, viewport.height)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
        spriteBatch.begin()

        val tip = controller.getLocalizedTip(state.step)
        val textHeight = getStringHeight(tip)

        spriteBatch.drawMultilineCentered(
            font = getFont(),
            str = tip,
            centerX = viewport.width / 2f,
            topY = viewport.height / TIP_Y_DIVISOR - textHeight / 2f,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
            color = Color(1f, 1f, 1f, textAlpha),
        )
    }

    companion object {
        private const val RENDER_LAYER = 100550
        private const val MAX_TICK_DELTA = 0.1f
        private const val DIM_ALPHA = 0.4f
        private const val TIP_Y_DIVISOR = 3f
    }
}
