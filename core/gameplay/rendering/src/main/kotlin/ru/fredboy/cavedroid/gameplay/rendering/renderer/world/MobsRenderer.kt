package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.performance.NoOpPerformanceMonitor
import ru.fredboy.cavedroid.common.performance.PerformanceGauge
import ru.fredboy.cavedroid.common.performance.PerformanceMetric
import ru.fredboy.cavedroid.common.performance.PerformanceMonitor
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class MobsRenderer @Inject constructor(
    private val performanceMonitor: PerformanceMonitor = NoOpPerformanceMonitor,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
    private val boats: BoatRenderer,
) : IWorldRenderer {

    override val renderLayer get() = RENDER_LAYER

    private var drawnMobs = 0L

    private val fireBlock: Block.Fire? by lazy { itemsRepository.getBlockByKey(FIRE_BLOCK_KEY) as? Block.Fire }

    private fun drawMob(spriteBatch: SpriteBatch, viewport: Rectangle, mob: Mob, delta: Float) {
        mob.hitbox.cycledInsideWorld(viewport, gameWorld.width.toFloat())?.let { mobRect ->
            drawnMobs++
            mob.draw(spriteBatch, mobRect.x, mobRect.y, delta)
            if (mob.isOnFire) {
                drawFireOverlay(spriteBatch, mobRect.x, mobRect.y, mob.width, mob.height)
            }
        }
    }

    private fun drawFireOverlay(spriteBatch: SpriteBatch, x: Float, y: Float, width: Float, height: Float) {
        val sprite = fireBlock?.sprite ?: return
        sprite.setBounds(x, y, width, height)
        sprite.draw(spriteBatch)
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        drawnMobs = 1
        val playerStart = performanceMonitor.startStage()
        boats.draw(spriteBatch, viewport, front = false)
        val player = mobController.player
        val playerDrawY = player.position.y - player.height / 2f + if (player.isMounted) SEATED_DRAW_OFFSET_Y else 0f
        player.draw(
            /* spriteBatch = */ spriteBatch,
            /* x = */ player.position.x - player.width,
            /* y = */ playerDrawY,
            /* delta = */ delta,
        )

        boats.draw(spriteBatch, viewport, front = true)

        if (player.isOnFire && !player.isDead) {
            drawFireOverlay(
                spriteBatch = spriteBatch,
                x = player.position.x - player.width / 2f,
                y = playerDrawY,
                width = player.width,
                height = player.height,
            )
        }

        performanceMonitor.endStage(PerformanceMetric.RENDER_PLAYER, playerStart)
        val mobsStart = performanceMonitor.startStage()
        mobController.mobs.forEach { mob ->
            drawMob(spriteBatch, viewport, mob, delta)
        }
        performanceMonitor.endStage(PerformanceMetric.RENDER_MOBS, mobsStart)
        performanceMonitor.incrementGauge(PerformanceGauge.ACTIVE_ENTITIES, mobController.mobs.size.toLong() + 1)
        performanceMonitor.incrementGauge(PerformanceGauge.DRAWN_ENTITIES, drawnMobs)
    }

    companion object {
        private const val RENDER_LAYER = 100100
        private const val FIRE_BLOCK_KEY = "fire"
        private const val SEATED_DRAW_OFFSET_Y = 0.375f
    }
}
