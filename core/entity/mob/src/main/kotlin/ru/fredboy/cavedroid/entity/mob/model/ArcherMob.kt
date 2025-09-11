package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.utils.applyOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class ArcherMob(
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
    params: MobParams,
    direction: Direction = Direction.random(),
) : WalkingMob(params, direction) {

    private fun drawBow(spriteBatch: SpriteBatch, x: Float, y: Float) {
        val item = getItemByKeyUseCase["bow"] as? Item.Bow ?: return
        val itemSprite = item.stateSprites[bowState]

        val originalWidth = itemSprite.width
        val originalHeight = itemSprite.height

        itemSprite.rotation = -45f + 90f * direction.index
        itemSprite.setFlip(looksRight(), itemSprite.isFlipY)
        itemSprite.setSize(1f, 1f)
        itemSprite.setOriginCenter()
        itemSprite.setOriginBasedPosition(x + width * direction.index, y)
        itemSprite.draw(spriteBatch)

        itemSprite.setFlip(false, itemSprite.isFlipY)
        itemSprite.rotation = 0f
        itemSprite.setOriginCenter()
        itemSprite.setSize(originalWidth, originalHeight)
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float,
    ) {
        updateAnimation(delta)

        val backgroundTintColor = tintColor.cpy().sub(Color(0xAAAAAA shl 8))

        params.sprites.forEach { spriteData ->
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

            if (spriteData.isHand && !spriteData.isBackground) {
                drawBow(
                    spriteBatch = spriteBatch,
                    x = x,
                    y = y + spriteData.offsetY,
                )
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
