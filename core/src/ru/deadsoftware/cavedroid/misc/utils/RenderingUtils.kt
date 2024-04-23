package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.misc.Assets

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
fun SpriteBatch.drawString(str: String, x: Float, y: Float, color: Color = Color.WHITE): GlyphLayout {
    Assets.minecraftFont.color = color
    return Assets.minecraftFont.draw(this, str, x, y)
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
