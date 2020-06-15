package ru.deadsoftware.cavedroid.game.objects

import com.badlogic.gdx.graphics.g2d.Sprite

data class Item(
        val name: String,
        val type: String,
        val sprite: Sprite?
) {

    init {
        sprite?.flip(false, true)
    }

    fun requireSprite() = sprite ?: throw IllegalStateException("Sprite is null")

    fun isBlock() = type == "block"

    @Deprecated("Was renamed to Sprite to comply with variable type.", ReplaceWith("requireSprite()"))
    fun getTexture() = sprite

}