package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject
import kotlin.math.min

@GameScope
@BindWorldRenderer
class ParallaxRenderer @Inject constructor(
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    private val parallaxPlainsRegion get() = requireNotNull(textureRegions["plains"])

    private val parallaxPlains = Sprite(parallaxPlainsRegion)

    override val renderLayer = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val parallaxWidth = parallaxPlains.regionWidth.meters / 2f
        val parallaxHeight = parallaxPlains.regionHeight.meters / 2f

        val repeatWidth = parallaxWidth * 2f

        var parallaxX = (-viewport.x * 0.5f) % repeatWidth
        if (parallaxX < 0f) {
            parallaxX += repeatWidth
        }
        parallaxX += viewport.x - parallaxWidth

        val seaLevel = gameWorld.generatorConfig.seaLevel.toFloat()
        val parallaxY = min(
            seaLevel,
            viewport.y + viewport.height * (1f - viewport.y / seaLevel),
        )

        parallaxPlains.setFlip(false, parallaxPlains.isFlipY)
        spriteBatch.drawSprite(
            sprite = parallaxPlains,
            x = parallaxX,
            y = parallaxY,
            width = parallaxWidth,
            height = parallaxHeight,
            tint = Color.LIGHT_GRAY,
        )

        parallaxPlains.setFlip(true, parallaxPlains.isFlipY)
        spriteBatch.drawSprite(
            sprite = parallaxPlains,
            x = parallaxX - parallaxWidth,
            y = parallaxY,
            width = parallaxWidth,
            height = parallaxHeight,
            tint = Color.LIGHT_GRAY,
        )

        spriteBatch.drawSprite(
            sprite = parallaxPlains,
            x = parallaxX + parallaxWidth,
            y = parallaxY,
            width = parallaxWidth,
            height = parallaxHeight,
            tint = Color.LIGHT_GRAY,
        )
    }

    companion object {
        private const val RENDER_LAYER = 99850
    }
}
