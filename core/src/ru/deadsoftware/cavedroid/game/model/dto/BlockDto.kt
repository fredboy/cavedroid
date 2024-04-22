package ru.deadsoftware.cavedroid.game.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockDto(
    @Deprecated("numeric ids will be removed") @SerialName("id") val id: Int? = null,
    @SerialName("left") val left: Int = 0,
    @SerialName("top") val top: Int = 0,
    @SerialName("right") val right: Int = 0,
    @SerialName("bottom") val bottom: Int = 0,
    @SerialName("sprite_left") val spriteLeft: Int = 0,
    @SerialName("sprite_top") val spriteTop: Int = 0,
    @SerialName("sprite_right") val spriteRight: Int = 0,
    @SerialName("sprite_bottom") val spriteBottom: Int = 0,
    @SerialName("hp") val hp: Int = -1,
    @SerialName("collision") val collision: Boolean = true,
    @SerialName("background") val background: Boolean = false,
    @SerialName("transparent") val transparent: Boolean = false,
    @SerialName("block_required") val blockRequired: Boolean = false,
    @SerialName("drop") val drop: String,
    @SerialName("meta") val meta: String? = null,
    @SerialName("texture") val texture: String,
    @SerialName("animated") val animated: Boolean = false,
    @SerialName("frames") val frames: Int = 0,
    @SerialName("drop_count") val dropCount: Int = 1,
    @SerialName("full_block") val fullBlock: String? = null,
    @SerialName("state") val state: Int? = null,
    @SerialName("other_part") val otherPart: String? = null,
    @SerialName("tool_level") val toolLevel: Int = 0,
    @SerialName("tool_type") val toolType: String? = null,
    @SerialName("damage") val damage: Int = 0,
)
