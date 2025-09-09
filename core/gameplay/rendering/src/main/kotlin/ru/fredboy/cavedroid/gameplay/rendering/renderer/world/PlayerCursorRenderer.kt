package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.MobTextureAssetsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class PlayerCursorRenderer @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val mobAssetsRepository: MobTextureAssetsRepository,
) : IWorldRenderer {

    override val renderLayer get() = RENDER_LAYER

    private fun drawCursor(spriteBatch: SpriteBatch) {
        val selectedX = mobController.player.selectedX
        val selectedY = mobController.player.selectedY

        if (gameWorld.hasForeAt(selectedX, selectedY) || gameWorld.hasBackAt(selectedX, selectedY)) {
            spriteBatch.draw(
                /* region = */ mobAssetsRepository.getPlayerCursorSprite(),
                /* x = */ selectedX.toFloat(),
                /* y = */ selectedY.toFloat(),
                /* width = */ 1f,
                /* height = */ 1f,
            )
        }
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        delta: Float,
    ) {
        drawCursor(spriteBatch)
    }

    companion object {
        private const val RENDER_LAYER = 100450
    }
}
