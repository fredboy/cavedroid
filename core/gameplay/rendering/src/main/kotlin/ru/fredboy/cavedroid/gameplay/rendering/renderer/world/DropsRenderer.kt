package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceGauge
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class DropsRenderer @Inject constructor(
    private val performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
    private val dropController: DropController,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    override val renderLayer = RENDER_LAYER

    private var resident = 0L
    private var drawn = 0L

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        resident = 0
        drawn = 0
        dropController.forEach { drop ->
            resident++
            drop.hitbox.cycledInsideWorld(viewport, gameWorld.width.toFloat())?.let { dropRect ->
                drawn++
                spriteBatch.drawSprite(
                    sprite = drop.item.sprite,
                    x = dropRect.x,
                    y = dropRect.y - MathUtils.sin(drop.bobTime) * .125f,
                    width = dropRect.width,
                    height = dropRect.height,
                )
            }
        }
        performanceMonitor.incrementGauge(PerformanceGauge.ACTIVE_ENTITIES, resident)
        performanceMonitor.incrementGauge(PerformanceGauge.DRAWN_ENTITIES, drawn)
    }

    companion object {
        private const val RENDER_LAYER = 100200
    }
}
