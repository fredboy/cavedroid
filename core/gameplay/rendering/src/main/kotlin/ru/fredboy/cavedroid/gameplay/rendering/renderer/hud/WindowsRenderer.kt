package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows.ChestWindowRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows.CraftingWindowRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows.CreativeTabsWindowRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows.CreativeWindowRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows.FurnaceWindowRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.windows.SurvivalWindowRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class WindowsRenderer @Inject constructor(
    private val creativeWindowRenderer: CreativeWindowRenderer,
    private val survivalWindowRenderer: SurvivalWindowRenderer,
    private val craftingWindowRenderer: CraftingWindowRenderer,
    private val gameWindowsManager: GameWindowsManager,
    private val furnaceWindowRenderer: FurnaceWindowRenderer,
    private val chestWindowRenderer: ChestWindowRenderer,
    private val creativeTabsWindowRenderer: CreativeTabsWindowRenderer,
) : IHudRenderer {

    override val renderLayer get() = RENDER_LAYER

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        // dim background
        if (gameWindowsManager.currentWindowType != GameWindowType.NONE) {
            spriteBatch.end()
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.setColor(0f, 0f, 0f, 0.4f)
            shapeRenderer.rect(0f, 0f, viewport.width, viewport.height)
            shapeRenderer.end()
            Gdx.gl.glDisable(GL20.GL_BLEND)
            spriteBatch.begin()
        }

        when (val windowType = gameWindowsManager.currentWindowType) {
            GameWindowType.CREATIVE_INVENTORY -> creativeWindowRenderer.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                viewport = viewport,
                delta = delta,
            )

            GameWindowType.SURVIVAL_INVENTORY -> survivalWindowRenderer.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                viewport = viewport,
                delta = delta,
            )

            GameWindowType.CRAFTING_TABLE -> craftingWindowRenderer.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                viewport = viewport,
                delta = delta,
            )

            GameWindowType.FURNACE -> furnaceWindowRenderer.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                viewport = viewport,
                delta = delta,
            )

            GameWindowType.CHEST -> chestWindowRenderer.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                viewport = viewport,
                delta = delta,
            )

            GameWindowType.CREATIVE_INVENTORY_TABS -> creativeTabsWindowRenderer.draw(
                spriteBatch = spriteBatch,
                shapeRenderer = shapeRenderer,
                viewport = viewport,
                delta = delta,
            )

            GameWindowType.NONE -> return

            else -> logger.e { "Cannot draw window: ${windowType.name}" }
        }
    }

    companion object {
        private const val TAG = "WindowsRenderer"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        const val RENDER_LAYER = 100800
    }
}
