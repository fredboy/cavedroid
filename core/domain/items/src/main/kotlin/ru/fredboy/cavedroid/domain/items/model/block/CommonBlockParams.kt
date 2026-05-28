package ru.fredboy.cavedroid.domain.items.model.block

import com.badlogic.gdx.graphics.Texture
import ru.fredboy.cavedroid.domain.items.model.drop.DropInfo
import ru.fredboy.cavedroid.domain.items.model.item.Item

data class CommonBlockParams(
    val key: String,
    val collisionMargins: BlockInsets.Meters,
    val hitPoints: Int,
    val dropInfo: List<DropInfo>,
    val dropsSingleItem: Boolean,
    val hasCollision: Boolean,
    val isBackground: Boolean,
    val isTransparent: Boolean,
    val requiresBlock: Boolean,
    val allowAttachToNeighbour: Boolean,
    val animationInfo: BlockAnimationInfo?,
    val texture: Texture?,
    val spriteMargins: BlockInsets.Pixels,
    val toolLevel: Int,
    val toolType: Class<out Item.Tool>?,
    val damage: Int,
    val tint: String?,
    val isFallable: Boolean,
    val castsShadows: Boolean,
    val lightInfo: BlockLightInfo?,
    val replaceable: Boolean,
    val material: BlockMaterial?,
    val actionSoundKey: String?,
    val combustible: Boolean,
    val requiresBackground: Boolean,
) {
    val spriteMarginsMeters by lazy { spriteMargins.toMeters() }

    val isFullBlock get() = collisionMargins.run { top == 0f && bottom == 0f && left == 0f && right == 0f }
}
