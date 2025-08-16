package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.MobAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class PlayerCursorRenderer @Inject constructor(
    private val gameWorld: GameWorld,
    private val textureRegions: GetTextureRegionByNameUseCase,
    private val mobController: MobController,
    private val mobAssetsRepository: MobAssetsRepository,
) : IWorldRenderer {

    override val renderLayer get() = RENDER_LAYER

    private fun drawCursor(spriteBatch: SpriteBatch) {
        val cursorX = mobController.player.cursorX
        val cursorY = mobController.player.cursorY

        if (gameWorld.hasForeAt(cursorX, cursorY) ||
            gameWorld.hasBackAt(cursorX, cursorY) ||
            mobController.player.controlMode == Player.ControlMode.CURSOR
        ) {
            spriteBatch.draw(mobAssetsRepository.getPlayerCursorSprite(), cursorX.toFloat(), cursorY.toFloat(), 1f, 1f)
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
