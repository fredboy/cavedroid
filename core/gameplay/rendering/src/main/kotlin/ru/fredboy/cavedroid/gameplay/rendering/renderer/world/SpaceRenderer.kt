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

    private val starField = StarField(gameWorld.generatorConfig.seed)

    // Cover the atmosphere (including weather), before drawing terrain and entities.
    override val renderLayer = 99900

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val top = maxOf(viewport.y, gameWorld.height.toFloat())
        val bottom = viewport.y + viewport.height
        if (top >= bottom) return

        spriteBatch.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0f, 0f, 0.1f, 1f)
        shapeRenderer.rect(viewport.x, top, viewport.width, bottom - top)

        for (star in starField.stars) {
            val x = viewport.x + star.x * viewport.width
            val y = viewport.y + star.y * viewport.height
            val starTop = maxOf(y, top)
            val starBottom = minOf(y + star.size, bottom)
            if (starTop >= starBottom) continue

            shapeRenderer.setColor(0.85f * star.brightness, 0.9f * star.brightness, star.brightness, 1f)
            shapeRenderer.rect(x, starTop, minOf(star.size, viewport.x + viewport.width - x), starBottom - starTop)
        }

        shapeRenderer.end()
        shapeRenderer.color = Color.WHITE
        spriteBatch.begin()
    }
}
