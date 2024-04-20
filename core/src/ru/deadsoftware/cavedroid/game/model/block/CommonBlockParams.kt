package ru.deadsoftware.cavedroid.game.model.block

import com.badlogic.gdx.graphics.Texture

data class CommonBlockParams(
    @Deprecated(ID_DEPRECATION_MESSAGE) val id: Int,
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
) {
    companion object {
        private const val ID_DEPRECATION_MESSAGE = "numeric id's will be removed"
    }
}
