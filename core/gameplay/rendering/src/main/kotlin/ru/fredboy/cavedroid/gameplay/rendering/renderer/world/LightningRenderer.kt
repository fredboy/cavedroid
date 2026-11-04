package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.WeatherLightning
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class LightningRenderer @Inject constructor(
    private val gameWorld: GameWorld,
    private val lightning: WeatherLightning,
) : IWorldRenderer {

    // Above the atmosphere and precipitation, behind space and all terrain layers.
    override val renderLayer = 99850

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        if (lightning.intensity <= 0f) return
        // The fixed underground background covers this region even without backMap blocks.
        // Clip explicitly so incomplete terrain caches cannot expose a flash below sea level.
        val bottom = minOf(viewport.y + viewport.height, gameWorld.generatorConfig.seaLevel.toFloat())
        if (bottom <= viewport.y) return

        spriteBatch.end()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0.85f, 0.9f, 1f, lightning.intensity * 0.45f)
        shapeRenderer.rect(viewport.x, viewport.y, viewport.width, bottom - viewport.y)
        shapeRenderer.end()
        shapeRenderer.color = Color.WHITE
        Gdx.gl.glDisable(GL20.GL_BLEND)
        spriteBatch.begin()
    }
}
