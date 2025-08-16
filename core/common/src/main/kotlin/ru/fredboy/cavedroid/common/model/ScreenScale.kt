package ru.fredboy.cavedroid.common.model

sealed interface ScreenScale {

    data object Auto : ScreenScale

    data class Value(val scale: Float) : ScreenScale {

        init {
            require(scale > 0) { "Scale must be positive." }
        }
    }
}
