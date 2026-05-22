package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawMultilineCentered
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindHudRenderer
import ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.IHudRenderer
import javax.inject.Inject

@GameScope
@BindHudRenderer
class TouchInventoryHintRenderer @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameWindowsManager: GameWindowsManager,
    private val fontTextureAssetsRepository: FontTextureAssetsRepository,
    private val inventoryHintController: InventoryHintController,
    private val getFont: GetFontUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
) : IHudRenderer {

    override val renderLayer: Int = RENDER_LAYER

    private val isTouch: Boolean = applicationContextRepository.isTouch()

    private val hintTexts by lazy {
        HINT_KEYS.map { key -> fontTextureAssetsRepository.getMenuLocalizationBundle()[key] }
    }

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        if (!isTouch || !inventoryHintController.isVisible) return
        val windowType = gameWindowsManager.currentWindowType
        if (windowType == GameWindowType.NONE || windowType == GameWindowType.CREATIVE_INVENTORY) return

        inventoryHintController.tick(delta)
        val alpha = inventoryHintController.alpha
        if (alpha <= 0f) return

        val hintText = hintTexts[inventoryHintController.hintIndex]
        val lines = hintText.split('\n')
        val lineHeight = getStringHeight(lines.first())
        val blockHeight = lines.size * lineHeight + (lines.size - 1).coerceAtLeast(0) * LINE_SPACING

        spriteBatch.drawMultilineCentered(
            font = getFont(),
            str = hintText,
            centerX = viewport.width / 2f,
            topY = viewport.height - blockHeight - BOTTOM_MARGIN,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
            color = Color(1f, 1f, 1f, alpha),
            lineSpacing = LINE_SPACING,
        )
    }

    companion object {
        private const val RENDER_LAYER = 100810
        private val HINT_KEYS = listOf("touchInventoryHint", "touchInventoryStackSplitHint")
        private const val BOTTOM_MARGIN = 8f
        private const val LINE_SPACING = 4f
    }
}
