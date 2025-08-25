package ru.fredboy.cavedroid.domain.items.model.mob

data class MobParams(
    val name: String,
    val key: String,
    val width: Float,
    val height: Float,
    val speed: Float,
    val behaviorType: MobBehaviorType,
    val dropInfo: MobDropInfo,
    val hp: Int,
    val sprites: List<MobSprite>,
    val animationRange: Float,
    val damageToPlayer: Int,
    val takesSunDamage: Boolean,
)
