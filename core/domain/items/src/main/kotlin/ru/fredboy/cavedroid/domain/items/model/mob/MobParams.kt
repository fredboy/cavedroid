package ru.fredboy.cavedroid.domain.items.model.mob

import ru.fredboy.cavedroid.domain.items.model.drop.DropInfo

data class MobParams(
    val name: String,
    val key: String,
    val width: Float,
    val height: Float,
    val speed: Float,
    val behaviorType: MobBehaviorType,
    val dropInfo: List<DropInfo>,
    val hp: Int,
    val sprites: List<MobSprite>,
    val animationRange: Float,
    val damageToPlayer: Int,
    val takesSunDamage: Boolean,
)
