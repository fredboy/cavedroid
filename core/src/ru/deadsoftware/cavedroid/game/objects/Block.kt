@file:Suppress("DeprecatedCallableAddReplaceWith")

package ru.deadsoftware.cavedroid.game.objects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameItems

private const val ANIMATION_FRAME_DURATION = 100L
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
 * @param texture       block's texture
 * @param animated      indicates if block has animation
 * @param frames        number of animation frames. ignored if animated is false
 * @param spriteLeft    block's sprite x on texture
 * @param spriteTop     block's sprite y on texture
 * @param spriteRight   block's sprite right edge on texture
 * @param spriteBottom  block's sprite bottom on texture
 */
data class Block(
        val id: Int,
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
        private val texture: Texture?,
        val animated: Boolean,
        val frames: Int,
        private val spriteLeft: Int,
        private val spriteTop: Int,
        private val spriteRight: Int,
        private val spriteBottom: Int
) {

    val width = 16 - right - left
    val height = 16 - top - bottom

    private val spriteWidth = 16 - spriteLeft - spriteRight
    private val spriteHeight = 16 - spriteTop - spriteBottom

    private val sprite: Sprite?
        get() {
            return if (animated) {
                animation[currentFrame()]
            } else {
                field
            }
        }


    private val animation: Array<Sprite>

    init {
        if (frames !in 0..Int.MAX_VALUE) {
            throw IllegalArgumentException("Animation frames must be in range [0, ${Int.MAX_VALUE}]")
        }

        animation = if (animated) {
            if (texture == null) {
                throw IllegalArgumentException("Cannot derive animation frames from null sprite")
            }
            Array(frames) { y ->
                Sprite(texture, spriteLeft, 16 * y + spriteTop, spriteWidth, spriteHeight).apply {
                    flip(false, true)
                }
            }
        } else {
            emptyArray()
        }

        sprite = if (animated) { animation[0] } else {
            if (texture != null) {
                Sprite(texture, spriteLeft, spriteTop, spriteWidth, spriteHeight).apply {
                    flip(false, true)
                }
            } else {
                null
            }
        }
    }

    private fun currentFrame() = if (animated) {
        ((System.currentTimeMillis() / ANIMATION_FRAME_DURATION) % frames).toInt()
    } else {
        0
    }

    fun requireSprite() = sprite ?: throw IllegalStateException("Sprite is null")

    fun draw(spriter: SpriteBatch, x: Float, y: Float) {
        requireSprite().apply {
            setBounds(x + spriteLeft, y + spriteTop, spriteWidth.toFloat(), spriteHeight.toFloat())
            draw(spriter)
        }
    }

    fun getRectangle(x: Int, y: Int) =
            Rectangle(x * 16f + left, y * 16f + this.top, width.toFloat(), height.toFloat())

    fun hasDrop() = drop != "none"

    fun toJump() = top < 8 && collision

    fun getItem() = GameItems.getItem(GameItems.getBlockKey(id))

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

    @Deprecated("Was renamed to Sprite to comply with variable type.", ReplaceWith("requireSprite()"))
    fun getTexture() = sprite

}