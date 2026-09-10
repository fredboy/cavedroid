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
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject
import kotlin.math.min

@GameScope
@BindWorldRenderer
class ParallaxRenderer @Inject constructor(
    textureRegions: GetTextureRegionByNameUseCase,
    private val gameWorld: GameWorld,
    private val player: PlayerAdapter,
) : IWorldRenderer {

    private val parallaxSprites = mapOf(
        Biome.PLAINS to Sprite(requireNotNull(textureRegions["plains"])),
        Biome.DESERT to Sprite(requireNotNull(textureRegions["desert"])),
        Biome.WINTER to Sprite(requireNotNull(textureRegions["winter"])),
    )

    private val tint = Color(Color.LIGHT_GRAY)

    override val renderLayer = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val plains = parallaxSprites.getValue(Biome.PLAINS)
        val parallaxWidth = plains.regionWidth.meters / 2f
        val parallaxHeight = plains.regionHeight.meters / 2f

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

        var accumulatedWeight = 0f
        for ((biome, sprite) in parallaxSprites) {
            val weight = gameWorld.biomeProximityFactor(
                centerX = player.x,
                rangeBlocks = BIOME_FADE_BLOCKS,
            ) { it == biome }
            if (weight <= 0f) continue

            accumulatedWeight += weight
            // Source-over blending yields normalized biome weights without exposing the sky.
            tint.a = weight / accumulatedWeight

            sprite.setFlip(false, sprite.isFlipY)
            spriteBatch.drawSprite(
                sprite = sprite,
                x = parallaxX,
                y = parallaxY,
                width = parallaxWidth,
                height = parallaxHeight,
                tint = tint,
            )

            sprite.setFlip(true, sprite.isFlipY)
            spriteBatch.drawSprite(
                sprite = sprite,
                x = parallaxX - parallaxWidth,
                y = parallaxY,
                width = parallaxWidth,
                height = parallaxHeight,
                tint = tint,
            )

            spriteBatch.drawSprite(
                sprite = sprite,
                x = parallaxX + parallaxWidth,
                y = parallaxY,
                width = parallaxWidth,
                height = parallaxHeight,
                tint = tint,
            )
        }
    }

    companion object {
        private const val RENDER_LAYER = 99850
        private const val BIOME_FADE_BLOCKS = 16f
    }
}
