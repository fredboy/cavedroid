package ru.deadsoftware.cavedroid.game.model.block

import com.badlogic.gdx.graphics.Texture

data class CommonBlockParams(
    @Deprecated("numeric id's will be removed") val id: Int,
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
)
