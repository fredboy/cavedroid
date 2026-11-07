package ru.fredboy.cavedroid.game.controller.waystone

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

/** Searches a small area at the destination; Y grows down and the returned point is the player's center. */
internal object WaystoneArrival {
    fun find(
        x: Int,
        y: Int,
        width: Float,
        height: Float,
        canonicalX: (Int) -> Int,
        safe: (Rectangle) -> Boolean,
        supported: (Rectangle) -> Boolean,
    ): Vector2? {
        for (dy in listOf(0, 1, -1, 2, -2)) {
            for (dx in listOf(0, -1, 1, -2, 2)) {
                val centerX = canonicalX(x + dx) + .5f
                val feetY = y + dy - .02f
                val bounds = Rectangle(centerX - width / 2f, feetY - height, width, height)
                if (safe(bounds) && supported(Rectangle(bounds.x, feetY, width, .12f))) {
                    return Vector2(centerX, feetY - height / 2f)
                }
            }
        }
        return null
    }
}
