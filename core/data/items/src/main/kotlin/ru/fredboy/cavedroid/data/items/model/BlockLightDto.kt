package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockLightDto(
    @SerialName("light_brightness") val lightBrightness: Float = 1f,
    @SerialName("light_distance") val lightDistance: Float,
)
