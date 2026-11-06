package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import ru.fredboy.cavedroid.gameplay.rendering.utils.PresentationPoses
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class MobsRenderer @Inject constructor(
    private val poses: PresentationPoses,
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
    private val boats: BoatRenderer,
) : IWorldRenderer {

    override val renderLayer get() = RENDER_LAYER

    private val playerPosition = Vector2()

    private val fireBlock: Block.Fire? by lazy { itemsRepository.getBlockByKey(FIRE_BLOCK_KEY) as? Block.Fire }

    private fun drawMob(spriteBatch: SpriteBatch, viewport: Rectangle, mob: Mob, delta: Float) {
        poses.bounds(mob.body, mob.hitbox).cycledInsideWorld(viewport, gameWorld.width.toFloat())?.let { mobRect ->
            mob.draw(spriteBatch, mobRect.x, mobRect.y, delta)
            if (mob.isOnFire) {
                drawFireOverlay(spriteBatch, mobRect.x, mobRect.y, mob)
            }
        }
    }

    private fun drawFireOverlay(spriteBatch: SpriteBatch, x: Float, y: Float, mob: Mob) {
        val sprite = fireBlock?.sprite ?: return
        val legs = mob.params.sprites.filter { it.isLeg && !it.isOverlay }
        val parts = when {
            legs.size >= 4 -> legs
            legs.isEmpty() -> mob.params.sprites.filter { it.isBody && !it.isOverlay }
            else -> emptyList()
        }
        val left = parts.minOfOrNull { it.offsetX } ?: 0f
        val right = parts.maxOfOrNull { it.offsetX + it.sprite.regionWidth.meters } ?: mob.width
        val fireWidth = right - left
        val offsetX = if (mob.looksRight()) mob.width - right else left
        sprite.setBounds(x + offsetX, y, fireWidth, mob.height)
        sprite.draw(spriteBatch)
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        boats.draw(spriteBatch, viewport, front = false)
        val player = mobController.player
        poses.playerPosition(playerPosition)
        val playerDrawY = playerPosition.y - player.height / 2f + if (player.isMounted) SEATED_DRAW_OFFSET_Y else 0f
        player.draw(
            /* spriteBatch = */ spriteBatch,
            /* x = */ playerPosition.x - player.width,
            /* y = */ playerDrawY,
            /* delta = */ delta,
        )

        boats.draw(spriteBatch, viewport, front = true)

        if (player.isOnFire && !player.isDead) {
            drawFireOverlay(
                spriteBatch = spriteBatch,
                x = playerPosition.x - player.width / 2f,
                y = playerDrawY,
                mob = player,
            )
        }

        mobController.mobs.forEach { mob ->
            drawMob(spriteBatch, viewport, mob, delta)
        }
    }

    companion object {
        private const val RENDER_LAYER = 100100
        private const val FIRE_BLOCK_KEY = "fire"
        private const val SEATED_DRAW_OFFSET_Y = 0.375f
    }
}
