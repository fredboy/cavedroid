package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
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
    private val mobController: MobController,
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
) : IWorldRenderer {

    override val renderLayer get() = RENDER_LAYER

    private val fireBlock: Block.Fire? by lazy { itemsRepository.getBlockByKey(FIRE_BLOCK_KEY) as? Block.Fire }

    private fun drawMob(spriteBatch: SpriteBatch, viewport: Rectangle, mob: Mob, delta: Float) {
        mob.hitbox.cycledInsideWorld(viewport, gameWorld.width.toFloat())?.let { mobRect ->
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
        val player = mobController.player
        player.draw(
            /* spriteBatch = */ spriteBatch,
            /* x = */ player.position.x - player.width,
            /* y = */ player.position.y - player.height / 2,
            /* delta = */ delta,
        )

        if (player.isOnFire && !player.isDead) {
            drawFireOverlay(
                spriteBatch = spriteBatch,
                x = player.position.x - player.width / 2f,
                y = player.position.y - player.height / 2f,
                width = player.width,
                height = player.height,
            )
        }

        mobController.mobs.forEach { mob ->
            drawMob(spriteBatch, viewport, mob, delta)
        }
    }

    companion object {
        private const val RENDER_LAYER = 100100
        private const val FIRE_BLOCK_KEY = "fire"
    }
}
