package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindRenderer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageFrameCountUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageSpriteUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindRenderer
class BackgroundBlocksRenderer @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockDamageFrameCount: GetBlockDamageFrameCountUseCase,
    getBlockDamageSprite: GetBlockDamageSpriteUseCase
) : BlocksRenderer(gameWorld, mobController, getBlockDamageFrameCount, getBlockDamageSprite) {

    override val renderLayer get() = RENDER_LAYER

    override val background = true

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        forEachBlockInArea(viewport) { x, y ->
            drawBackMap(spriteBatch, viewport, x, y)
        }

        drawBlockDamage(spriteBatch, viewport)

        spriteBatch.end()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.setColor(0f, 0f, 0f, .5f)

        forEachBlockInArea(viewport) { x, y ->
            shadeBackMap(shapeRenderer, viewport, x, y)
        }

        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
        spriteBatch.begin()

        forEachBlockInArea(viewport) { x, y ->
            drawForeMap(spriteBatch, viewport, x, y)
        }
    }

    companion object {
        private const val RENDER_LAYER = 100000
    }
}