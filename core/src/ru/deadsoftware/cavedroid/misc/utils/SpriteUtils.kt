package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * Draw sprite at given position rotated by [rotation] degrees
 */
@JvmOverloads
fun SpriteBatch.drawSprite(
    sprite: Sprite,
    x: Float,
    y: Float,
    rotation: Float = 0f,
    width: Float = sprite.regionWidth.toFloat(),
    height: Float = sprite.regionHeight.toFloat(),
) {
    sprite.setPosition(x, y)
    sprite.setSize(width, height)
    sprite.rotation = rotation
    sprite.draw(this)

    sprite.setSize(sprite.regionWidth.toFloat(), sprite.regionHeight.toFloat())
    sprite.rotation = 0f
}

fun Sprite.applyOrigin(origin: SpriteOrigin) {
    origin.applyToSprite(this)
}