package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.utils.applyOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.abstraction.MobBehavior
import ru.fredboy.cavedroid.entity.mob.impl.AggressiveMobBehavior
import ru.fredboy.cavedroid.entity.mob.impl.PassiveMobBehavior

open class WalkingMob(
    params: MobParams,
    direction: Direction = Direction.random(),
) : Mob(direction, params, mapMobBehavior(params.behaviorType)) {

    override fun changeDir() {
        switchDir()
        controlVector.set(Vector2(direction.basis * speed, 0f))
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

    companion object {
        private const val TAG = "WalkingMob"

        private fun mapMobBehavior(behaviorType: MobBehaviorType): MobBehavior {
            return when (behaviorType) {
                MobBehaviorType.PASSIVE, MobBehaviorType.SHEEP -> PassiveMobBehavior()
                MobBehaviorType.AGGRESSIVE -> AggressiveMobBehavior()
                else -> {
                    Gdx.app.error(TAG, "$behaviorType is not supported for Walking mob. Spawning as Passive")
                    PassiveMobBehavior()
                }
            }
        }
    }
}
