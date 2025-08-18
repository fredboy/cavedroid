package ru.fredboy.cavedroid.domain.items.model.mob

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.common.model.SpriteOrigin

data class MobSprite(
    val sprite: Sprite,
    val offsetX: Float,
    val offsetY: Float,
    val isBackground: Boolean,
    val isHand: Boolean,
    val isHead: Boolean,
    val isStatic: Boolean,
    val origin: SpriteOrigin,
)
