package ru.fredboy.cavedroid.ux.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.ux.rendering.annotation.BindRenderer
import ru.fredboy.cavedroid.ux.rendering.windows.ChestWindowRenderer
import ru.fredboy.cavedroid.ux.rendering.windows.CraftingWindowRenderer
import ru.fredboy.cavedroid.ux.rendering.windows.CreativeWindowRenderer
import ru.fredboy.cavedroid.ux.rendering.windows.FurnaceWindowRenderer
import ru.fredboy.cavedroid.ux.rendering.windows.SurvivalWindowRenderer
import javax.inject.Inject

@GameScope
@BindRenderer
class WindowsRenderer @Inject constructor(
    private val creativeWindowRenderer: CreativeWindowRenderer,
    private val survivalWindowRenderer: SurvivalWindowRenderer,
    private val craftingWindowRenderer: CraftingWindowRenderer,
    private val gameWindowsManager: GameWindowsManager,
    private val furnaceWindowRenderer: FurnaceWindowRenderer,
    private val chestWindowRenderer: ChestWindowRenderer,
) : IGameRenderer {

    override val renderLayer get() = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        when (val windowType = gameWindowsManager.currentWindowType) {
            GameWindowType.CREATIVE_INVENTORY -> creativeWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameWindowType.SURVIVAL_INVENTORY -> survivalWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameWindowType.CRAFTING_TABLE -> craftingWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameWindowType.FURNACE -> furnaceWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameWindowType.CHEST -> chestWindowRenderer.draw(spriteBatch, shapeRenderer, viewport, delta)
            GameWindowType.NONE -> return
            else -> Gdx.app.error(TAG, "Cannot draw window: ${windowType.name}")
        }
    }

    companion object {
        private const val TAG = "WindowsRenderer"

        const val RENDER_LAYER = 100600
    }
}
