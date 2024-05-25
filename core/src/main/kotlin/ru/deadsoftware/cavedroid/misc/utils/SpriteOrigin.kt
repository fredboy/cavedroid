package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.graphics.g2d.Sprite

/**
 * An origin of a [com.badlogic.gdx.graphics.g2d.Sprite]
 *
 * x and y must be between 0 and 1 in percents from sprite size
 */
data class SpriteOrigin(
    val x: Float,
    val y: Float,
) {

    init {
        assert(x in 0f..1f)
        assert(y in 0f..1f)
    }

    fun getFlipped(flipX: Boolean, flipY: Boolean): SpriteOrigin {
        return SpriteOrigin(
            x = if (flipX) 1 - x else x,
            y = if (flipY) 1 - y else y,
        )
    }

    fun applyToSprite(sprite: Sprite) {
        sprite.setOrigin(sprite.width * x, sprite.height * y)
    }

}
