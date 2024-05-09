package ru.deadsoftware.cavedroid.game.model.item

import ru.deadsoftware.cavedroid.misc.utils.SpriteOrigin

data class CommonItemParams(
    val key: String,
    val name: String,
    val inHandSpriteOrigin: SpriteOrigin,
    val maxStack: Int,
)