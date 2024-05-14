package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.objects.drop.DropController
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindRenderer
import ru.deadsoftware.cavedroid.misc.utils.cycledInsideWorld
import ru.deadsoftware.cavedroid.misc.utils.drawSprite
import ru.deadsoftware.cavedroid.misc.utils.px
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