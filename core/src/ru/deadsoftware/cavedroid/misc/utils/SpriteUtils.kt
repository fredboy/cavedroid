package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * Draw sprite at given position rotated by [rotation] degrees
 */
@JvmOverloads
fun SpriteBatch.draw(sprite: Sprite, x: Float, y: Float, rotation: Float = 0f) {
    sprite.rotation = rotation
    sprite.setPosition(x, y)
    sprite.draw(this)
}

fun Sprite.applyOrigin(origin: SpriteOrigin) {
    setOrigin(width * origin.x, height * origin.y)
}