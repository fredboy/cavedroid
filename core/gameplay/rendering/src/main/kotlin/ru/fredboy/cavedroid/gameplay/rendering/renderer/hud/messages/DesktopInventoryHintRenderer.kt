package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TutorialTapTargets
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding.TutorialToastRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class DesktopInventoryHintRenderer @Inject constructor(
    private val application: ApplicationContextRepository,
    private val windows: GameWindowsManager,
    private val controller: InventoryHintController,
    private val fonts: FontTextureAssetsRepository,
    private val toast: TutorialToastRenderer,
    private val tapTargets: TutorialTapTargets,
) : IHudRenderer {
    override val renderLayer: Int = 100810

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        tapTargets.clear(TAP_KEY)
        val window = windows.currentWindow
        if (application.isTouch() || window == null || windows.currentWindowType == GameWindowType.CREATIVE_INVENTORY) return
        if (!controller.isVisible) return
        controller.tick(delta)
        if (!controller.isVisible) return
        val index = controller.hintIndex
        val text = fonts.getMenuLocalizationBundle()[HINT_KEYS[index]]
        val bounds = toast.draw(spriteBatch, viewport, text, controller.alpha)
        tapTargets.register(TAP_KEY, bounds) {
            if (windows.currentWindow === window && controller.isVisible && controller.hintIndex == index) {
                controller.dismiss()
                true
            } else {
                false
            }
        }
    }

    companion object {
        private const val TAP_KEY = "desktopInventory"
        private val HINT_KEYS = listOf("inventoryMoveHint", "inventoryPlaceOneHint", "inventorySplitHint")
    }
}
