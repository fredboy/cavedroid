package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.GameUiWindow
import ru.deadsoftware.cavedroid.game.render.windows.CraftingWindowRenderer
import ru.deadsoftware.cavedroid.game.render.windows.CreativeWindowRenderer
import ru.deadsoftware.cavedroid.game.render.windows.FurnaceWindowRenderer
import ru.deadsoftware.cavedroid.game.render.windows.SurvivalWindowRenderer
import ru.deadsoftware.cavedroid.game.ui.windows.GameWindowsManager
import javax.inject.Inject

@GameScope
class WindowsRenderer @Inject constructor(
    private val creativeWindowRenderer: CreativeWindowRenderer,
    private val survivalWindowRenderer: SurvivalWindowRenderer,
    private val craftingWindowRenderer: CraftingWindowRenderer,
    private val gameWindowsManager: GameWindowsManager,
    private val furnaceWindowRenderer: FurnaceWindowRenderer,
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        when (val windowType = gameWindowsManager.getCurrentWindow()) {
            GameUiWindow.CREATIVE_INVENTORY -> creativeWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameUiWindow.SURVIVAL_INVENTORY -> survivalWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameUiWindow.CRAFTING_TABLE -> craftingWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameUiWindow.FURNACE -> furnaceWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameUiWindow.NONE -> return
            else -> Gdx.app.error(TAG, "Cannot draw window: ${windowType.name}")
        }
    }

    companion object {
        private const val TAG = "WindowsRenderer"

        const val RENDER_LAYER = 100600
    }
}