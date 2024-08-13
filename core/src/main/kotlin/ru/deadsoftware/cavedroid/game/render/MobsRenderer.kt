package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindRenderer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.model.Mob
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindRenderer
class MobsRenderer @Inject constructor(
    private val mobController: MobController,
    private val gameWorld: GameWorld,
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

    private fun drawMob(spriteBatch: SpriteBatch, viewport: Rectangle, mob: Mob, delta: Float) {
         mob.cycledInsideWorld(viewport, gameWorld.width.px)?.let { mobRect ->
             mob.draw(spriteBatch, mobRect.x - viewport.x, mobRect.y - viewport.y, delta)
         }
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        val player = mobController.player
        player.draw(
            /* spriteBatch = */ spriteBatch,
            /* x = */ player.x - viewport.x - player.width / 2,
            /* y = */ player.y - viewport.y,
            /* delta = */ delta
        )

        mobController.mobs.forEach { mob ->
            drawMob(spriteBatch, viewport, mob, delta)
        }
    }

    companion object {
        private const val RENDER_LAYER = 100100
    }
}