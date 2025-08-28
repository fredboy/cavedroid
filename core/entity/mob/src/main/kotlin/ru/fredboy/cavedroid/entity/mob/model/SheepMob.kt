package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.utils.applyOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class SheepMob(
    params: MobParams,
    direction: Direction = Direction.random(),
) : WalkingMob(params, direction) {

    var hasFur: Boolean = true

    override fun getDropItems(itemByKey: GetItemByKeyUseCase): List<InventoryItem> {
        return if (hasFur) {
            super.getDropItems(itemByKey)
        } else {
            emptyList()
        }
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float,
    ) {
        updateAnimation(delta)

        val backgroundTintColor = tintColor.cpy().sub(Color(0xAAAAAA shl 8))

        params.sprites.filter { hasFur || !it.isOverlay }.forEach { spriteData ->
            val sprite = spriteData.sprite

            sprite.setFlip(looksRight(), sprite.isFlipY)
            sprite.applyOrigin(spriteData.origin.getFlipped(looksRight(), false))

            if (spriteData.isBackground) {
                sprite.color = backgroundTintColor
            } else {
                sprite.color = tintColor
            }

            val animationValue = if (spriteData.isStatic) {
                0f
            } else if (spriteData.isBackground) {
                anim
            } else {
                -anim
            }

            spriteBatch.drawSprite(
                sprite = sprite,
                x = x +
                    (width - sprite.width - spriteData.offsetX) * direction.index +
                    spriteData.offsetX * (1 - direction.index),
                y = y + spriteData.offsetY,
                rotation = animationValue,
            )
        }
    }
}
