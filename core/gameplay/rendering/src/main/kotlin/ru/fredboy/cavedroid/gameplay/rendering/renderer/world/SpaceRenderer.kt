package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class SpaceRenderer @Inject constructor(
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    private val gradientTopColor = Color()

    // Cover the atmosphere (including weather), before drawing terrain and entities.
    override val renderLayer = 99900

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val top = maxOf(viewport.y, gameWorld.height.toFloat())
        val bottom = viewport.y + viewport.height
        if (top >= bottom) return

        spriteBatch.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLACK
        shapeRenderer.rect(viewport.x, top, viewport.width, bottom - top)

        // Anchor the fade to world depth so moving the camera doesn't change the void's color.
        val fadeEnd = gameWorld.height + FADE_DEPTH
        if (top < fadeEnd) {
            val depth = (top - gameWorld.height) / FADE_DEPTH
            gradientTopColor.set(BORDER_COLOR).lerp(Color.BLACK, depth)
            shapeRenderer.rect(
                viewport.x,
                top,
                viewport.width,
                fadeEnd - top,
                gradientTopColor,
                gradientTopColor,
                Color.BLACK,
                Color.BLACK,
            )
        }

        shapeRenderer.end()
        shapeRenderer.color = Color.WHITE
        spriteBatch.begin()
    }

    companion object {
        private const val FADE_DEPTH = 32f
        private val BORDER_COLOR = Color.valueOf("100d0c")
    }
}
