package ru.fredboy.cavedroid.data.assets.model.button

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TouchButtonDto(
    @SerialName("x") val x: Float,
    @SerialName("y") val y: Float,
    @SerialName("w") val width: Float,
    @SerialName("h") val height: Float,
    @SerialName("mouse") val isMouse: Boolean = false,
    @SerialName("key") val key: String,
)
