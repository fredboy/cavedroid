package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class CloudsRenderer @Inject constructor(gameWorld: GameWorld) : IWorldRenderer {

    private val cloudField = CloudField(gameWorld.generatorConfig.seed, gameWorld.generatorConfig.seaLevel)

    override val renderLayer = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        spriteBatch.end()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.9f, 0.9f, 0.95f, 0.2f)
        cloudField.forEachVisibleCloud(viewport) { cloud, x, y ->
            shapeRenderer.rect(x, y, cloud.width, cloud.height)
        }
        shapeRenderer.end()
        shapeRenderer.color = Color.WHITE
        Gdx.gl.glDisable(GL20.GL_BLEND)
        spriteBatch.begin()
    }

    companion object {
        // Above the sun, moon and stars, behind the parallax landscape.
        private const val RENDER_LAYER = 99725
    }
}
