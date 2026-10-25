package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.model.SpriteOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams

class VillagerMob(params: MobParams) : WalkingMob(params) {
    data class BlockPosition(val x: Int, val y: Int)

    /** Left half of the resident's bed. A missing home is allowed for spawn-egg villagers. */
    var home: BlockPosition? = null
    var isSleeping = false
    val openedDoors = mutableSetOf<BlockPosition>()

    override fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float) {
        if (!isSleeping) {
            super.draw(spriteBatch, x, y, delta)
            return
        }
        // Rotate the assembled body onto the mattress, with the head at the right-hand pillow.
        val right = x + width / 2f + height / 2f
        val top = y + height - 0.5f - width
        params.sprites.forEach { part ->
            part.sprite.setFlip(false, part.sprite.isFlipY)
            part.sprite.color = if (part.isBackground) tintColor.cpy().sub(BG_BODY_TINT) else tintColor
            spriteBatch.drawSprite(
                sprite = part.sprite,
                x = right - part.offsetY,
                y = top + part.offsetX,
                rotation = 90f,
                origin = SpriteOrigin(0f, 0f),
            )
        }
    }
}
