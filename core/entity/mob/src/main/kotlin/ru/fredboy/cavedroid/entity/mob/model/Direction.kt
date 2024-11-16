package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.math.MathUtils

enum class Direction(
    val index: Int,
    val basis: Int,
) {
    LEFT(0, -1),
    RIGHT(1, 1);

    companion object {
        fun random() = if (MathUtils.randomBoolean()) LEFT else RIGHT
    }
}
