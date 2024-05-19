package ru.deadsoftware.cavedroid.game.model.block

import com.badlogic.gdx.graphics.Texture
import ru.deadsoftware.cavedroid.game.model.item.Item

data class CommonBlockParams(
    val key: String,
    val collisionMargins: BlockMargins,
    val hitPoints: Int,
    val dropInfo: BlockDropInfo?,
    val hasCollision: Boolean,
    val isBackground: Boolean,
    val isTransparent: Boolean,
    val requiresBlock: Boolean,
    val animationInfo: BlockAnimationInfo?,
    val texture: Texture?,
    val spriteMargins: BlockMargins,
    val toolLevel: Int,
    val toolType: Class<out Item.Tool>?,
    val damage: Int,
    val tint: String?,
    val isFallable: Boolean,
)
