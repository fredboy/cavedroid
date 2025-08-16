package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.cycledInsideWorld
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class DropsRenderer @Inject constructor(
    private val dropController: DropController,
    private val gameWorld: GameWorld,
) : IWorldRenderer {

    override val renderLayer = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        dropController.forEach { drop ->
            drop.hitbox.cycledInsideWorld(viewport, gameWorld.width.toFloat())?.let { dropRect ->
                spriteBatch.drawSprite(
                    sprite = drop.item.sprite,
                    x = dropRect.x,
                    y = dropRect.y - MathUtils.sin(drop.bobTime) * .125f,
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
