package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.model.SpriteOrigin
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceGauge
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.game.controller.projectile.ProjectileController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject
import kotlin.math.atan2

@GameScope
@BindWorldRenderer
class ProjectilesRenderer @Inject constructor(
    private val performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
    private val projectileController: ProjectileController,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    override val renderLayer get() = RENDER_LAYER

    private var resident = 0L
    private var drawn = 0L

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        resident = 0
        drawn = 0
        projectileController.projectiles.forEach { projectile ->
            resident++
            projectile.hitbox.cycledInsideWorld(viewport, gameWorld.width.toFloat())?.let { projectileRect ->
                drawn++
                val rotationOffset = atan2(
                    y = projectileRect.height,
                    x = projectileRect.width,
                ) * MathUtils.radiansToDegrees

                spriteBatch.drawSprite(
                    sprite = projectile.item.sprite,
                    x = projectileRect.x,
                    y = projectileRect.y,
                    width = projectileRect.width,
                    height = projectileRect.height,
                    rotation = projectile.velocity.get().angleDeg() + rotationOffset,
                    origin = SpriteOrigin.CENTER,
                )
            }
        }
        performanceMonitor.incrementGauge(PerformanceGauge.ACTIVE_ENTITIES, resident)
        performanceMonitor.incrementGauge(PerformanceGauge.DRAWN_ENTITIES, drawn)
    }

    companion object {
        private const val RENDER_LAYER = 100250
    }
}
