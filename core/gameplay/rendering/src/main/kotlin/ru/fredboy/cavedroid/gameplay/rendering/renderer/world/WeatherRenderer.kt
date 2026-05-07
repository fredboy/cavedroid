package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

@GameScope
@BindWorldRenderer
class WeatherRenderer @Inject constructor(
    private val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepositoryTexture,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    override val renderLayer: Int
        get() = RENDER_LAYER

    private var scrollOffsetMeters: Float = 0f

    override fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        delta: Float,
    ) {
        drawSunAndMoon(spriteBatch, viewport)

        scrollOffsetMeters = (scrollOffsetMeters + delta * SCROLL_SPEED_M_PER_SEC) % TEXTURE_PERIOD_METERS

        if (gameWorld.weatherIntensity > 0f) {
            drawPrecipitation(spriteBatch, viewport)
        }
    }

    private fun drawSunAndMoon(spriteBatch: SpriteBatch, viewport: Rectangle) {
        val skyWidth = max(SKY_SPAN, viewport.width)
        val normalizedTime = gameWorld.getNormalizedTime()
        val sunPositionX = (skyWidth * normalizedTime) + (viewport.width / 2 - skyWidth / 2)
        val moonPositionX = (sunPositionX + skyWidth) % (skyWidth * 2f)
        val sunSprite = environmentTextureRegionsRepository.getSunSprite()
        val moonSprite = environmentTextureRegionsRepository.getMoonPhaseSprite(gameWorld.moonPhase)

        sunSprite.setOriginCenter()

        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
        spriteBatch.drawSprite(
            sprite = sunSprite,
            x = viewport.x + sunPositionX - sunSprite.width / 2,
            y = viewport.y + viewport.height * 0.25f - sunSprite.height / 2,
        )
        spriteBatch.drawSprite(
            sprite = moonSprite,
            x = viewport.x + moonPositionX - moonSprite.width / 2,
            y = viewport.y + viewport.height * 0.25f - moonSprite.height / 2,
        )
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun drawPrecipitation(spriteBatch: SpriteBatch, viewport: Rectangle) {
        val rainTexture = environmentTextureRegionsRepository.getRainTexture()
        val snowTexture = environmentTextureRegionsRepository.getSnowTexture()
        val alpha = gameWorld.weatherIntensity.coerceIn(0f, 1f)

        val firstTileY = floor((viewport.y - scrollOffsetMeters) / TEXTURE_PERIOD_METERS) *
            TEXTURE_PERIOD_METERS + scrollOffsetMeters
        val viewportBottom = viewport.y + viewport.height

        val xStart = floor(viewport.x).toInt()
        val xEnd = ceil(viewport.x + viewport.width).toInt()

        val oldColor = spriteBatch.packedColor
        spriteBatch.setColor(1f, 1f, 1f, alpha)

        for (xi in xStart..xEnd) {
            val texture: Texture = when (gameWorld.getBiomeAt(xi)) {
                Biome.PLAINS -> rainTexture
                Biome.WINTER -> snowTexture
                Biome.DESERT -> continue
            }
            val variant = xi.mod(TEXTURE_VARIANTS)
            val u0 = variant.toFloat() / TEXTURE_VARIANTS
            val u1 = u0 + 1f / TEXTURE_VARIANTS

            var y = firstTileY
            while (y < viewportBottom) {
                spriteBatch.draw(
                    /* texture = */ texture,
                    /* x = */ xi.toFloat(),
                    /* y = */ y,
                    /* width = */ 1f,
                    /* height = */ TEXTURE_PERIOD_METERS,
                    /* u = */ u0,
                    /* v = */ 0f,
                    /* u2 = */ u1,
                    /* v2 = */ 1f,
                )
                y += TEXTURE_PERIOD_METERS
            }
        }

        spriteBatch.packedColor = oldColor
    }

    companion object {
        private const val RENDER_LAYER = 99800

        private const val TEXTURE_PERIOD_METERS = 16f
        private const val TEXTURE_VARIANTS = 4
        private const val SCROLL_SPEED_M_PER_SEC = 6f

        private val SKY_SPAN = 480.meters
    }
}
