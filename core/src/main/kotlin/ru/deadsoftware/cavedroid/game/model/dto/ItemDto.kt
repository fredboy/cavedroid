package ru.deadsoftware.cavedroid.game.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    @SerialName("name") val name: String,
    @SerialName("type") val type: String = "normal",
    @SerialName("texture") val texture: String,
    @SerialName("origin_x") val originX: Float = 0f,
    @SerialName("origin_y") val origin_y: Float = 1f,
    @SerialName("action_key") val actionKey: String? = null,
    @SerialName("mob_damage_multiplier") val mobDamageMultiplier: Float = 1f,
    @SerialName("block_damage_multiplier") val blockDamageMultiplier: Float = 1f,
    @SerialName("top_slab_block") val topSlabBlock: String? = null,
    @SerialName("bottom_slab_block") val bottomSlabBlock: String? = null,
    @SerialName("tool_level") val toolLevel: Int? = null,
    @SerialName("max_stack") val maxStack: Int = 64,
    @SerialName("tint") val tint: String? = null,
    @SerialName("burning_time") val burningTime: Long? = null,
    @SerialName("smelt_product") val smeltProduct: String? = null,
    @SerialName("heal") val heal: Int? = null,
)
