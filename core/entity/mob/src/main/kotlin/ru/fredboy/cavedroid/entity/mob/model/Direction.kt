package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

enum class Direction(
    val index: Int,
    val basis: Int,
) {
    LEFT(0, -1),
    RIGHT(1, 1),
    ;

    companion object {
        fun random() = if (MathUtils.randomBoolean()) LEFT else RIGHT

        fun fromVector(vec: Vector2): Direction {
            return if (vec.x < 0) {
                Direction.LEFT
            } else {
                Direction.RIGHT
            }
        }
    }
}
