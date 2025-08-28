package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobSpriteDto(
    @SerialName("file") val file: String,
    @SerialName("is_background") val isBackground: Boolean,
    @SerialName("is_head") val isHead: Boolean = false,
    @SerialName("is_hand") val isHand: Boolean = false,
    @SerialName("is_static") val isStatic: Boolean = true,
    @SerialName("is_overlay") val isOverlay: Boolean = false,
    @SerialName("offset_x") val offsetX: Float = 0f,
    @SerialName("offset_y") val offsetY: Float = 0f,
    @SerialName("origin_x") val originX: Float = 0.5f,
    @SerialName("origin_y") val originY: Float = 0f,
)
