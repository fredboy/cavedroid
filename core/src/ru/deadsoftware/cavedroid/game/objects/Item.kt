package ru.deadsoftware.cavedroid.game.objects

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.deadsoftware.cavedroid.game.GameItems
import ru.deadsoftware.cavedroid.misc.utils.SpriteOrigin

data class Item(
        val id: Int,
        val key: String,
        val name: String,
        val type: String,
        val sprite: Sprite?,
        val defaultOrigin: SpriteOrigin,
) {

    init {
        sprite?.flip(false, true)
    }

    fun requireSprite() = sprite ?: throw IllegalStateException("Sprite is null")

    fun isBlock() = type == "block"

    fun isTool() = type == "tool"

    /**
     * Returns block associated with this item. Null if this is not a block
     */
    fun toBlock(): Block? {
        if (!isBlock()) {
            return null
        }

        return GameItems.getBlock(GameItems.getBlockIdByItemId(id))
    }

    fun getItemOrBlockSprite(): Sprite {
        return requireNotNull(sprite ?: toBlock()?.requireSprite()) { "wtf: sprite is null" }
    }

    fun isNone(): Boolean {
        return id == 0;
    }

    @Deprecated("Was renamed to Sprite to comply with variable type.", ReplaceWith("requireSprite()"))
    fun getTexture() = sprite
}