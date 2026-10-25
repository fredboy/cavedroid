package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.game.controller.explosion.ExplosionController
import ru.fredboy.cavedroid.game.controller.explosion.ExplosionRules
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

@GameScope
@BindWorldRenderer
class ExplosionsRenderer @Inject constructor(
    private val explosions: ExplosionController,
    private val items: GetItemByKeyUseCase,
    private val textures: GetTextureRegionByNameUseCase,
    private val gameWorld: GameWorld,
) : IWorldRenderer {
    override val renderLayer = 100300

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val color = spriteBatch.packedColor
        val src = spriteBatch.blendSrcFunc
        val dst = spriteBatch.blendDstFunc
        val srcAlpha = spriteBatch.blendSrcFuncAlpha
        val dstAlpha = spriteBatch.blendDstFuncAlpha
        spriteBatch.setColor(1f, 1f, 1f, 1f)
        explosions.primedTnt.forEach { tnt ->
            drawRegion(spriteBatch, items["tnt"].sprite, tnt.x, tnt.y, 0.98f, viewport)
            if ((tnt.fuse * 2f).toInt() % 2 == 0) {
                // Additive pass brightens the existing sprite without needing a separate white texture.
                spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
                spriteBatch.setColor(1f, 1f, 1f, 0.8f)
                drawRegion(spriteBatch, items["tnt"].sprite, tnt.x, tnt.y, 0.98f, viewport)
                spriteBatch.setBlendFunctionSeparate(src, dst, srcAlpha, dstAlpha)
                spriteBatch.setColor(1f, 1f, 1f, 1f)
            }
            repeat(3) { puff ->
                val progress = ((ExplosionRules.FUSE_SECONDS - tnt.fuse) * 1.5f + puff / 3f) % 1f
                spriteBatch.setColor(0.6f, 0.6f, 0.6f, 1f - progress)
                val frame = (progress * 12).toInt().coerceIn(0, 11)
                textures["big_smoke_$frame"]?.let {
                    drawRegion(spriteBatch, it, tnt.x + sin(progress * 4f + puff) * 0.15f, tnt.y - 0.5f - progress, 0.4f, viewport)
                }
            }
            spriteBatch.setColor(1f, 1f, 1f, 1f)
        }
        explosions.effects.forEach { explosion ->
            val progress = (explosion.age / ExplosionRules.EFFECT_SECONDS).coerceIn(0f, 0.999f)
            val frame = (progress * 16).toInt()
            textures["explosion_$frame"]?.let { texture ->
                drawRegion(spriteBatch, texture, explosion.x, explosion.y, 3f + progress * 2f, viewport)
                repeat(6) { puff ->
                    val angle = puff * Math.PI.toFloat() / 3f
                    val spread = progress * 3f
                    drawRegion(
                        spriteBatch,
                        texture,
                        explosion.x + cos(angle) * spread,
                        explosion.y + sin(angle) * spread,
                        2f,
                        viewport,
                    )
                }
            }
        }
        spriteBatch.packedColor = color
    }

    private fun drawRegion(batch: SpriteBatch, region: TextureRegion, x: Float, y: Float, size: Float, viewport: Rectangle) {
        val bounds = Rectangle(x - size / 2f, y - size / 2f, size, size)
        val visible = if (gameWorld.isInfinite) {
            bounds.takeIf { viewport.overlaps(it) }
        } else {
            bounds.cycledInsideWorld(viewport, gameWorld.width.toFloat())
        } ?: return
        batch.draw(region, visible.x, visible.y, size, size)
    }
}
