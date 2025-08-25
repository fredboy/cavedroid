package ru.fredboy.cavedroid.data.items.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobParamsDto(
    @SerialName("name") val name: String,
    @SerialName("width") val width: Float,
    @SerialName("height") val height: Float,
    @SerialName("speed") val speed: Float,
    @SerialName("behavior") val type: String,
    @SerialName("drop") val drop: String,
    @SerialName("drop_count") val dropCount: Int = 1,
    @SerialName("hp") val hp: Int = -1,
    @SerialName("animation_range") val animationRange: Float = 60f,
    @SerialName("damage_to_player") val damageToPlayer: Int = 0,
    @SerialName("takes_sun_damage") val takesSunDamage: Boolean = false,
    @SerialName("sprites") val sprites: List<MobSpriteDto>,
)
