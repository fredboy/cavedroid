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
) {
    val oldColor = sprite.color

    sprite.setPosition(x, y)
    sprite.setSize(width, height)
    sprite.rotation = rotation
    tint?.let(sprite::setColor)

    sprite.draw(this)

    sprite.setSize(sprite.regionWidth.meters, sprite.regionHeight.meters)
    sprite.rotation = 0f
    sprite.color = oldColor
}

fun Sprite.applyOrigin(origin: SpriteOrigin) {
    origin.applyToSprite(this)
}
