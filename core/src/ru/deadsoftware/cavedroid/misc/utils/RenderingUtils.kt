package ru.deadsoftware.cavedroid.misc.utils

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
    func: (x: Int, y: Int) -> Unit
) {
    val startMapX = area.x.bl
    val endMapX = startMapX + area.width.bl + 1
    val startMapY = area.y.bl
    val endMapY = startMapY + area.height.bl + 1

    for (x in startMapX..endMapX) {
        for (y in startMapY..endMapY) {
            func(x, y)
        }
    }
}