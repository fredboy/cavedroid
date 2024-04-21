package ru.deadsoftware.cavedroid.game.model.item

import ru.deadsoftware.cavedroid.misc.utils.SpriteOrigin

data class CommonItemParams(
    @Deprecated("numeric id's will be removed") val id: Int?,
    val key: String,
    val name: String,
    val inHandSpriteOrigin: SpriteOrigin,
)