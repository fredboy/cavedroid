package ru.fredboy.cavedroid.domain.items.model.block

import com.badlogic.gdx.graphics.Texture
import ru.fredboy.cavedroid.domain.items.model.item.Item

data class CommonBlockParams(
    val key: String,
    val collisionMargins: BlockInsets.Meters,
    val hitPoints: Int,
    val dropInfo: BlockDropInfo?,
    val hasCollision: Boolean,
    val isBackground: Boolean,
    val isTransparent: Boolean,
    val requiresBlock: Boolean,
    val animationInfo: BlockAnimationInfo?,
    val texture: Texture?,
    val spriteMargins: BlockInsets.Pixels,
    val toolLevel: Int,
    val toolType: Class<out Item.Tool>?,
    val damage: Int,
    val tint: String?,
    val isFallable: Boolean,
    val castsShadows: Boolean,
) {
    val spriteMarginsMeters by lazy { spriteMargins.toMeters() }
}
