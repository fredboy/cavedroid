package ru.fredboy.cavedroid.ux.rendering

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.ux.rendering.annotation.BindRenderer
import javax.inject.Inject

@GameScope
@BindRenderer
class DropsRenderer @Inject constructor(
    private val dropController: DropController,
    private val gameWorld: GameWorld,
) : IGameRenderer {

    override val renderLayer = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        dropController.forEach { drop ->
            drop.cycledInsideWorld(viewport, gameWorld.width.px)?.let { dropRect ->
                spriteBatch.drawSprite(
                    sprite = drop.item.sprite,
                    x = dropRect.x - viewport.x,
                    y = dropRect.y - viewport.y,
                    width = dropRect.width,
                    height = dropRect.height,
                )
            }
        }
    }

    companion object {
        private const val RENDER_LAYER = 100200
    }

}