@file:Suppress("DeprecatedCallableAddReplaceWith")

package ru.deadsoftware.cavedroid.game.objects

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Rectangle

private const val DEPRECATION_MESSAGE =
        "Deprecated since moved to Kotlin. Use generated getter or kotlin property access."

/**
 * @param left          margin from left edge
 * @param top           margin from top edge
 * @param right         margin from right edge
 * @param bottom        margin from bottom edge
 * @param hp            hit points
 * @param drop          id of an item the block will drop when destroyed
 * @param collision     true if block has collision
 * @param background    true if block should be drawn behind player
 * @param transparent   true if block is transparent and renderer should draw a block behind it
 * @param requiresBlock true if block should break when there is no block with collision under it
 * @param fluid         true if fluid
 * @param meta          extra info for storing
 * @param sprite        block's texture
 */
data class Block(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
        val hp: Int,
        val drop: String,
        val collision: Boolean,
        val background: Boolean,
        val transparent: Boolean,
        val requiresBlock: Boolean,
        val fluid: Boolean,
        val meta: String,
        val sprite: Sprite?
) {

    init {
        sprite?.flip(false, true)
    }

    val width = 16 - right - left
    val height = 16 - top - bottom

    fun getRectangle(x: Int, y: Int) =
            Rectangle(x * 16f + left, y * 16f + this.top, width.toFloat(), height.toFloat())

    fun requireSprite() = sprite ?: throw IllegalStateException("Sprite is null")

    fun hasDrop() = drop != "none"

    fun toJump() = top < 8 && collision

    @Deprecated(DEPRECATION_MESSAGE)
    fun hasCollision() = collision

    @Deprecated(DEPRECATION_MESSAGE)
    fun isBackground() = background

    @Deprecated(DEPRECATION_MESSAGE)
    fun isTransparent() = transparent

    @Deprecated(DEPRECATION_MESSAGE)
    fun isFluid() = fluid

    @Deprecated(DEPRECATION_MESSAGE)
    fun requiresBlock() = requiresBlock

    @Deprecated("Was renamed to Sprite to comply with variable type.", ReplaceWith("getSprite()"))
    fun getTexture() = sprite

}