package ru.fredboy.cavedroid.game.controller.vehicle

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.entity.vehicle.BoatTuning

/** Deterministic exit search; callers supply collision and support queries. */
object VehicleSpace {
    fun canPlace(clear: Boolean, supported: Boolean, waterArea: Float): Boolean = clear && (supported || waterArea > 0f)

    fun overlaps(bounds: Rectangle, obstacle: Rectangle, worldWidth: Float?): Boolean {
        if (bounds.overlaps(obstacle)) return true
        if (worldWidth == null) return false
        val nearestX = obstacle.x + kotlin.math.round((bounds.x - obstacle.x) / worldWidth) * worldWidth
        return bounds.overlaps(Rectangle(nearestX, obstacle.y, obstacle.width, obstacle.height))
    }

    fun findExit(
        x: Float,
        y: Float,
        facing: Int,
        width: Float,
        height: Float,
        clear: (Rectangle) -> Boolean,
        supported: (Rectangle) -> Boolean,
        water: (Rectangle) -> Boolean,
        allowAbove: Boolean = false,
    ): Vector2? {
        val above = Rectangle(x - width / 2f, y - BoatTuning.HEIGHT / 2f - height - 0.05f, width, height)
        val candidates = buildList {
            for (side in listOf(facing, -facing)) {
                for (rise in listOf(0f, 0.5f, 1f)) {
                    add(
                        Rectangle(
                            x + side * (BoatTuning.WIDTH / 2f + width / 2f + 0.1f) - width / 2f,
                            y + BoatTuning.HEIGHT / 2f - height - rise,
                            width,
                            height,
                        ),
                    )
                }
            }
            add(above)
        }.filter(clear)
        val exit = candidates.firstOrNull(supported) ?: candidates.firstOrNull(water)
            ?: above.takeIf { allowAbove && it in candidates } ?: return null
        return Vector2(exit.x + width / 2f, exit.y + height / 2f)
    }
}
