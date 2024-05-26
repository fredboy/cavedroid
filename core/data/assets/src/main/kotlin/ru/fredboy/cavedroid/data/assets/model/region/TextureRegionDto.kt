package ru.fredboy.cavedroid.data.assets.model.region

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TextureRegionDto(
    @SerialName("x") val x: Int = 0,
    @SerialName("y") val y: Int = 0,
    @SerialName("w") val width: Int? = null,
    @SerialName("h") val height: Int? = null,
)
