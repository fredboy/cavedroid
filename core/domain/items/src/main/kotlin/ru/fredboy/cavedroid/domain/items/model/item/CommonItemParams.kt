package ru.fredboy.cavedroid.domain.items.model.item

import ru.fredboy.cavedroid.common.model.SpriteOrigin

data class CommonItemParams(
    val key: String,
    val name: String,
    val inHandSpriteOrigin: SpriteOrigin,
    val maxStack: Int,
    val burningTimeMs: Long?,
    val smeltProductKey: String?,
)
