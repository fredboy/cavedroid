package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.render.windows.CreativeWindowRenderer
import ru.deadsoftware.cavedroid.game.render.windows.SurvivalWindowRenderer
import javax.inject.Inject

@GameScope
class WindowsRenderer @Inject constructor(
    private val mainConfig: MainConfig,
    private val creativeWindowRenderer: CreativeWindowRenderer,
    private val survivalWindowRenderer: SurvivalWindowRenderer,
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        when (mainConfig.gameUiWindow) {
            GameUiWindow.CREATIVE_INVENTORY -> creativeWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameUiWindow.SURVIVAL_INVENTORY -> survivalWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameUiWindow.NONE -> return
            else -> Gdx.app.error(TAG, "Cannot draw window: ${mainConfig.gameUiWindow.name}")
        }
    }

    companion object {
        private const val TAG = "WindowsRenderer"

        const val RENDER_LAYER = 100600
    }
}