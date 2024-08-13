package ru.deadsoftware.cavedroid.game.render

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
class ForegroundBlocksRenderer @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    getBlockDamageFrameCount: GetBlockDamageFrameCountUseCase,
    getBlockDamageSprite: GetBlockDamageSpriteUseCase
) : BlocksRenderer(gameWorld, mobController, getBlockDamageFrameCount, getBlockDamageSprite) {

    override val renderLayer get() = RENDER_LAYER

    override val background = false

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        forEachBlockInArea(viewport) { x, y ->
            drawForeMap(spriteBatch, viewport, x, y)
        }
        drawBlockDamage(spriteBatch, viewport)
    }

    companion object {
        private const val RENDER_LAYER = 100400
    }
}