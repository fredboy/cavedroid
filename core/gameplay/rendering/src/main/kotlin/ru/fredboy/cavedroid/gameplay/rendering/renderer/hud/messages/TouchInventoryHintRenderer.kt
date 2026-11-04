package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.TutorialTapTargets
import ru.fredboy.cavedroid.common.utils.drawMultilineCentered
import ru.fredboy.cavedroid.common.utils.safeCast
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem.Companion.isNoneOrNull
import ru.fredboy.cavedroid.game.window.GameWindowType
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import ru.fredboy.cavedroid.game.window.TouchWindowLayout
import ru.fredboy.cavedroid.game.window.inventory.CreativeInventoryTabsWindow
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
    private val tapTargets: TutorialTapTargets,
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
        tapTargets.clear("touchInventory")
        if (!isTouch || !inventoryHintController.isVisible) return
        val windowType = gameWindowsManager.currentWindowType
        if (windowType == GameWindowType.NONE || windowType == GameWindowType.CREATIVE_INVENTORY) return
        if (gameWindowsManager.currentWindow?.safeCast<CreativeInventoryTabsWindow>()?.selectedTab?.isInventory == false) {
            return
        }

        if (!gameWindowsManager.currentWindow?.selectedItem.isNoneOrNull()) return
        inventoryHintController.tick(delta)
        if (!inventoryHintController.isVisible) return
        val alpha = inventoryHintController.alpha
        if (alpha <= 0f) return

        val layout = TouchWindowLayout(viewport.width, viewport.height, requireNotNull(gameWindowsManager.currentWindow))
        val bounds = layout.bounds
        val scale = bounds.width / TouchWindowLayout.WIDTH
        val width = TouchWindowLayout.WIDTH
        val height = layout.designHeight
        val maxWidth = width - TouchWindowLayout.DROP_WIDTH - 16f
        val lines = mutableListOf<String>()
        var line = ""
        hintTexts[inventoryHintController.hintIndex].split(Regex("\\s+")).forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (line.isNotEmpty() && getStringWidth(candidate) > maxWidth) {
                lines += line
                line = word
            } else {
                line = candidate
            }
        }
        if (line.isNotEmpty()) lines += line
        val lineHeight = getStringHeight(lines.first())
        val blockHeight = lines.size * lineHeight + (lines.size - 1).coerceAtLeast(0) * 2f
        val window = gameWindowsManager.currentWindow
        val index = inventoryHintController.hintIndex
        val footer = Rectangle(
            bounds.x,
            bounds.y + (height - TouchWindowLayout.FOOTER_HEIGHT) * scale,
            (width - TouchWindowLayout.DROP_WIDTH) * scale,
            TouchWindowLayout.FOOTER_HEIGHT * scale,
        )
        tapTargets.register("touchInventory", footer) {
            if (gameWindowsManager.currentWindow === window && inventoryHintController.isVisible &&
                inventoryHintController.hintIndex == index && window?.selectedItem.isNoneOrNull()
            ) {
                inventoryHintController.dismiss()
                true
            } else {
                false
            }
        }
        val oldTransform = Matrix4(spriteBatch.transformMatrix)
        spriteBatch.transformMatrix = Matrix4(oldTransform).translate(bounds.x, bounds.y, 0f).scale(scale, scale, 1f)
        try {
            spriteBatch.drawMultilineCentered(
                font = getFont(),
                str = lines.joinToString("\n"),
                centerX = (width - TouchWindowLayout.DROP_WIDTH) / 2,
                topY = height - TouchWindowLayout.FOOTER_HEIGHT + (TouchWindowLayout.FOOTER_HEIGHT - blockHeight) / 2,
                getStringWidth = getStringWidth::invoke,
                getStringHeight = getStringHeight::invoke,
                color = Color(1f, 1f, 1f, alpha),
                lineSpacing = 2f,
            )
        } finally {
            spriteBatch.transformMatrix = oldTransform
        }
    }

    companion object {
        private const val RENDER_LAYER = 100810
        private val HINT_KEYS = listOf(
            "touchInventoryDragHint",
            "touchInventoryPlaceOneHint",
            "touchInventoryStackSplitHint",
        )
    }
}
