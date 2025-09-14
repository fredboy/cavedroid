package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.model.SpriteOrigin

/**
 * Draw sprite at given position rotated by [rotation] degrees
 */
@JvmOverloads
fun SpriteBatch.drawSprite(
    sprite: Sprite,
    x: Float,
    y: Float,
    rotation: Float = 0f,
    width: Float = sprite.regionWidth.meters,
    height: Float = sprite.regionHeight.meters,
    tint: Color? = null,
    origin: SpriteOrigin? = null,
) {
    val oldColor = sprite.color.cpy()

    sprite.setPosition(x, y)
    sprite.setSize(width, height)
    origin?.applyToSprite(sprite)
    sprite.rotation = rotation
    tint?.let { sprite.color = sprite.color.mul(tint) }

    sprite.draw(this)

    sprite.setSize(sprite.regionWidth.meters, sprite.regionHeight.meters)
    sprite.rotation = 0f
    sprite.color = oldColor
    sprite.setOriginCenter()
}

fun Sprite.applyOrigin(origin: SpriteOrigin) {
    origin.applyToSprite(this)
}
