package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class ProjectilesRenderer @Inject constructor(
    private val projectileController: ProjectileController,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    override val renderLayer get() = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        projectileController.projectiles.forEach { projectile ->
            projectile.hitbox.cycledInsideWorld(viewport, gameWorld.width.toFloat())?.let { projectileRect ->
                spriteBatch.drawSprite(
                    sprite = projectile.item.sprite,
                    x = projectileRect.x,
                    y = projectileRect.y,
                    width = projectileRect.width,
                    height = projectileRect.height,
                )
            }
        }
    }

    companion object {
        private const val RENDER_LAYER = 100250
    }
}
