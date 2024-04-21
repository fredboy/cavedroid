package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameInput
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.ControlMode
import ru.deadsoftware.cavedroid.misc.utils.ArrayMapExtensions.component1
import ru.deadsoftware.cavedroid.misc.utils.ArrayMapExtensions.component2
import javax.inject.Inject

@GameScope
class TouchControlsRenderer @Inject constructor(
    private val gameInput: GameInput
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

    private val shadeTexture get() = Assets.textureRegions[SHADE_KEY]

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val touchControlsMap = Assets.guiMap

        touchControlsMap.forEach { (key, value) ->
            val touchKey = value.rect
            spriteBatch.draw(
                /* region = */ Assets.textureRegions[key],
                /* x = */ touchKey.x,
                /* y = */ touchKey.y,
                /* width = */ touchKey.width,
                /* height = */ touchKey.height
            )
        }

        // FIXME: Add pressed state for buttons
        if (gameInput.controlMode == ControlMode.CURSOR) {
            val altKeyRect = touchControlsMap.get("alt").rect
            spriteBatch.draw(shadeTexture, altKeyRect.x, altKeyRect.y, altKeyRect.width, altKeyRect.height)
        }
    }

    companion object {
        private const val RENDER_LAYER = 100700

        private const val SHADE_KEY = "shade"
    }

}