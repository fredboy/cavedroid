package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.utils.applyOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.impl.PassiveMobBehavior
import kotlin.unaryMinus

class PassiveMob(
    params: MobParams,
    direction: Direction = Direction.random(),
) : Mob(direction, params, PassiveMobBehavior()) {

    override fun changeDir() {
        switchDir()
        controlVector.set(Vector2(direction.basis * speed, 0f))
    }

    override fun damage(damage: Int) {
        super.damage(damage)

        if (damage > 0) {
            if (canJump) {
                jump()
            }
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

        params.sprites.forEach { spriteData ->
            val sprite = spriteData.sprite

            sprite.setFlip(looksRight(), sprite.isFlipY)
            sprite.applyOrigin(spriteData.origin)

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
