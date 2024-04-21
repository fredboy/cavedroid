package ru.deadsoftware.cavedroid.game.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    @Deprecated("numeric ids will be removed") @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("texture") val texture: String,
    @SerialName("origin_x") val originX: Float = 0f,
    @SerialName("origin_y") val origin_y: Float = 1f,
    @SerialName("action_key") val actionKey: String? = null,
    @SerialName("mob_damage_multiplier") val mobDamageMultiplier: Float = 1f,
    @SerialName("block_damage_multiplier") val blockDamageMultiplier: Float = 1f,
    @SerialName("top_slab_block") val topSlabBlock: String? = null,
    @SerialName("bottom_slab_block") val bottomSlabBlock: String? = null,
)
