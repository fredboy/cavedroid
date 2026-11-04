package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.onboarding

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.drawMultilineCentered
import ru.fredboy.cavedroid.domain.assets.usecase.GetFontUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringHeightUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetStringWidthUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import javax.inject.Inject

@GameScope
class TutorialToastRenderer @Inject constructor(
    private val getFont: GetFontUseCase,
    private val getStringWidth: GetStringWidthUseCase,
    private val getStringHeight: GetStringHeightUseCase,
    private val textureRegions: GetTextureRegionByNameUseCase,
) {
    private val background by lazy { NinePatch(requireNotNull(textureRegions["onboarding_toast"]), 8, 8, 8, 8) }

    fun draw(batch: SpriteBatch, viewport: Rectangle, text: String, alpha: Float): Rectangle {
        val maxWidth = (viewport.width - 40f).coerceAtLeast(1f)
        val lines = mutableListOf<String>()
        text.split('\n').forEach { paragraph ->
            var line = ""
            paragraph.split(Regex("\\s+")).forEach { word ->
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (line.isNotEmpty() && getStringWidth(candidate) > maxWidth) {
                    lines += line
                    line = word
                } else {
                    line = candidate
                }
            }
            lines += line
        }
        val lineHeight = getStringHeight(lines.first())
        val height = lines.size * lineHeight + (lines.size - 1) * 4f + 20f
        val width = (lines.maxOf { getStringWidth(it) } + 24f).coerceAtLeast(40f)
        val bounds = Rectangle((viewport.width - width) / 2f, viewport.height - height - 12f, width, height)
        background.color = Color(1f, 1f, 1f, alpha)
        background.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height)
        batch.drawMultilineCentered(
            font = getFont(),
            str = lines.joinToString("\n"),
            centerX = viewport.width / 2f,
            topY = bounds.y + 10f,
            getStringWidth = getStringWidth::invoke,
            getStringHeight = getStringHeight::invoke,
            color = Color(1f, 1f, 1f, alpha),
        )
        return bounds
    }
}
