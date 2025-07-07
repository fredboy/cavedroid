package ru.fredboy.cavedroid.domain.items.model.block

import ru.fredboy.cavedroid.common.utils.meters

sealed interface BlockInsets<T : Number> {

    val left: T
    val top: T
    val right: T
    val bottom: T

    data class Pixels(
        override val left: Int,
        override val top: Int,
        override val right: Int,
        override val bottom: Int,
    ) : BlockInsets<Int> {

        fun toMeters(): Meters {
            return Meters(
                left = left.meters,
                top = top.meters,
                right = right.meters,
                bottom = bottom.meters,
            )
        }
    }

    data class Meters(
        override val left: Float,
        override val top: Float,
        override val right: Float,
        override val bottom: Float,
    ) : BlockInsets<Float>
}
