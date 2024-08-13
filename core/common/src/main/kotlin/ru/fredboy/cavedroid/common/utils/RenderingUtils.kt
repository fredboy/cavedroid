package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle

private fun Rectangle.shifted(shift: Float) = Rectangle(x + shift, y, width, height)

private fun Rectangle.getLazyShifts(worldWidthPx: Float)
    = Triple(
        first = lazy { shifted(0f) },
        second = lazy { shifted(-worldWidthPx) },
        third = lazy { shifted(worldWidthPx) }
    )

fun Rectangle.cycledInsideWorld(
    viewport: Rectangle,
    worldWidthPx: Float,
): Rectangle? {
    val (notShifted, shiftedLeft, shiftedRight) = getLazyShifts(worldWidthPx)

    return when {
        viewport.overlaps(notShifted.value) -> notShifted.value
        viewport.overlaps(shiftedLeft.value) -> shiftedLeft.value
        viewport.overlaps(shiftedRight.value) -> shiftedRight.value
        else -> null
    }
}

fun forEachBlockInArea(
    area: Rectangle,
    func: (x: Int, y: Int) -> Unit,
) {
    val startMapX = area.x.bl
    val endMapX = (area.x + area.width - 1f).bl
    val startMapY = area.y.bl
    val endMapY = (area.y + area.height - 1f).bl

    for (x in startMapX..endMapX) {
        for (y in startMapY..endMapY) {
            func(x, y)
        }
    }
}

@JvmOverloads
fun SpriteBatch.drawString(
    font: BitmapFont,
    str: String,
    x: Float,
    y: Float,
    color: Color = Color.WHITE
): GlyphLayout {
    font.color = color
    return font.draw(this, str, x, y)
}

/**
 * Parses hex color string into [Color]
 * Format is strictly #FFFFFF
 */
fun colorFromHexString(hex: String): Color {
    if (hex[0] != '#' || hex.length != 7) {
        return Color.WHITE
    }

    var rgba = try {
        hex.substring(1).toInt(16)
    } catch (e: NumberFormatException) {
        0xffffff
    }

    rgba = (rgba shl 8) or 0xFF
    return Color(rgba)
}

fun SpriteBatch.withScissors(
    viewportWidth: Float,
    viewportHeight: Float,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    block: () -> Unit
) {
    val scaleX = Gdx.graphics.width / viewportWidth
    val scaleY = Gdx.graphics.height / viewportHeight

    flush()
    Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
    Gdx.gl.glScissor(
        /* x = */ (x * scaleX).toInt(),
        /* y = */ ((viewportHeight - y - height) * scaleY).toInt(),
        /* width = */ (width * scaleX).toInt(),
        /* height = */ (height * scaleY).toInt()
    )
    block.invoke()
    flush()
    Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
}
