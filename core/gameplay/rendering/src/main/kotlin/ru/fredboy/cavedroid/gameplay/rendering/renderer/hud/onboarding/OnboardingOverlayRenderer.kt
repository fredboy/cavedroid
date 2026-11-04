package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TutorialTapTargets
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class OnboardingOverlayRenderer @Inject constructor(
    private val controller: OnboardingController,
    private val gameWindowsManager: GameWindowsManager,
    private val toast: TutorialToastRenderer,
    private val tapTargets: TutorialTapTargets,
) : IHudRenderer {
    override val renderLayer: Int = 100700

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        tapTargets.clear(TAP_KEY)
        if (gameWindowsManager.currentWindowType != GameWindowType.NONE) return
        val state = controller.visibleState ?: return
        val alpha = if (state.awaitingInput) 1f else (1f - state.cooldownProgress).coerceIn(0f, 1f)
        val bounds = toast.draw(spriteBatch, viewport, controller.getLocalizedTip(state.step), alpha)
        tapTargets.register(TAP_KEY, bounds) {
            if (gameWindowsManager.currentWindowType == GameWindowType.NONE && controller.visibleState === state) {
                controller.dismissVisibleTip()
                true
            } else {
                false
            }
        }
    }

    companion object {
        private const val TAP_KEY = "onboarding"
    }
}
