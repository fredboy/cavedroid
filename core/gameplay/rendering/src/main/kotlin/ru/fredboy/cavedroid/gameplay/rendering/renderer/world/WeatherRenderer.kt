package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepository
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject
import kotlin.math.max

@GameScope
@BindWorldRenderer
class WeatherRenderer @Inject constructor(
    private val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepository,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    override val renderLayer: Int
        get() = RENDER_LAYER

    override fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        delta: Float,
    ) {
        val skyWidth = max(SKY_SPAN, viewport.width)
        val normalizedTime = gameWorld.getNormalizedTime()
        val moonPositionX = (skyWidth * (normalizedTime - 0.5f)) + (viewport.width / 2 - skyWidth / 2)
        val sunPositionX = (moonPositionX + skyWidth) % (skyWidth * 2f)
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

    companion object {
        private const val RENDER_LAYER = 99800

        private val SKY_SPAN = 480.meters
    }
}
