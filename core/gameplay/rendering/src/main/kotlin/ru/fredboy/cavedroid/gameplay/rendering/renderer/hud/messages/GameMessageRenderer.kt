package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

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
class GameMessageRenderer @Inject constructor(
    private val controller: GameMessageController,
    private val getFont: GetFontUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
) : IHudRenderer {

    override val renderLayer: Int = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val message = controller.currentMessage ?: return
        val textHeight = getStringHeight(message)

        spriteBatch.drawMultilineCentered(
            font = getFont(),
            str = message,
            centerX = viewport.width / 2f,
            topY = viewport.height / TIP_Y_DIVISOR - textHeight / 2f,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
        )
    }

    companion object {
        private const val RENDER_LAYER = 100540
        private const val TIP_Y_DIVISOR = 3f
    }
}
