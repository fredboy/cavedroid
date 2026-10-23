package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject
import kotlin.math.max

@GameScope
@BindWorldRenderer
class SunAndMoonRenderer @Inject constructor(
    private val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepositoryTexture,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    private val starField = StarField(gameWorld.generatorConfig.seed)

    override val renderLayer: Int
        get() = RENDER_LAYER

    override fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        delta: Float,
    ) {
        val sunSprite = environmentTextureRegionsRepository.getSunSprite()
        val moonSprite = environmentTextureRegionsRepository.getMoonPhaseSprite(gameWorld.moonPhase)
        val sunWidth = sunSprite.regionWidth.meters
        val moonWidth = moonSprite.regionWidth.meters
        val skyBodyWidth = max(sunWidth, moonWidth)
        val skyWidth = max(SKY_SPAN, viewport.width) + skyBodyWidth * 2
        val totalSpan = skyWidth * 2f
        val normalizedTime = gameWorld.getNormalizedTime()
        // Avoid `(sunPositionX + skyWidth) % totalSpan` — TeaVM's Float.rem codegen
        // drops the parentheses around the divisor and evaluates it as
        // `((sunPositionX + skyWidth) % skyWidth) * 2f`, so the moon ends up
        // tracking `2 * sunPositionX` and is visible all day.
        val rawSunPos = (skyWidth * normalizedTime) + (viewport.width / 2 - skyWidth / 2)
        val sunPositionX = rawSunPos - skyBodyWidth + ((skyBodyWidth - sunWidth) / 2f)
        val rawMoonPos = rawSunPos + skyWidth
        val moonPositionX = (if (rawMoonPos >= totalSpan) rawMoonPos - totalSpan else rawMoonPos) -
            skyBodyWidth + ((skyBodyWidth - moonWidth) / 2f)

        drawStars(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            viewport = viewport,
            moonX = moonPositionX + moonWidth / 2f,
            moonY = viewport.height * 0.25f,
            moonRadius = moonWidth * MOON_DISC_RADIUS_RATIO,
        )

        sunSprite.setOriginCenter()

        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        spriteBatch.drawSprite(
            sprite = sunSprite,
            x = viewport.x + sunPositionX,
            y = viewport.y + viewport.height * 0.25f - sunSprite.regionHeight.meters / 2,
        )
        spriteBatch.drawSprite(
            sprite = moonSprite,
            x = viewport.x + moonPositionX,
            y = viewport.y + viewport.height * 0.25f - moonSprite.regionHeight.meters / 2,
        )
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun drawStars(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        moonX: Float,
        moonY: Float,
        moonRadius: Float,
    ) {
        val opacity = StarField.opacity(gameWorld.getSunlight())
        if (opacity <= 0f) return

        spriteBatch.end()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (star in starField.stars) {
            val x = star.x * viewport.width
            val y = star.y * viewport.height
            // The moon atlas has an opaque black background and uses additive blending.
            // It cannot cover stars, even on the lit side. Mask the entire disc for every phase.
            if (StarField.overlapsMoon(x, y, star.size, moonX, moonY, moonRadius)) continue

            shapeRenderer.setColor(0.85f, 0.9f, 1f, opacity * star.brightness)
            shapeRenderer.rect(viewport.x + x, viewport.y + y, star.size, star.size)
        }
        shapeRenderer.end()
        shapeRenderer.color = Color.WHITE
        Gdx.gl.glDisable(GL20.GL_BLEND)
        spriteBatch.begin()
    }

    companion object {
        private const val RENDER_LAYER = 99700
        private val SKY_SPAN = 480.meters

        // Each 96px tile has a central 16px-radius disc. Include its pixelated outer rim.
        private const val MOON_DISC_RADIUS_RATIO = 18f / 96f
    }
}
