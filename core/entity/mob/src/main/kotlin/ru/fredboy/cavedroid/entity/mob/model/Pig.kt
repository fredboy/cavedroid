package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.assets.model.MobSprite
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.entity.mob.abstraction.MobBehavior

class Pig(
    private val sprite: MobSprite.Pig,
    x: Float,
    y: Float,
    behavior: MobBehavior,
) : Mob(x, y, WIDTH, HEIGHT, Direction.random(), MAX_HEALTH, behavior) {

    override val speed get() = SPEED
    
    override fun changeDir() {
        switchDir()
        velocity = Vector2(direction.basis * speed, 0f)
    }

    override fun jump() {
        velocity.y = JUMP_VELOCITY
    }

    override fun damage(damage: Int) {
        super.damage(damage)

        if (damage > 0) {
            if (canJump) {
                jump()
            }
        }
    }

    override fun getDropItems(itemByKey: GetItemByKeyUseCase): List<InventoryItem> {
        return listOf(itemByKey["porkchop_raw"].toInventoryItem())
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float
    ) {
        updateAnimation(delta)

        val leftLegX = x + sprite.getLeftLegRelativeX(direction.index)
        val rightLegX = x + sprite.getRightLegRelativeX(direction.index)
        val legY = y + sprite.getLegsRelativeY()

        sprite.leg.setOrigin(sprite.leg.width / 2, 0f)
        sprite.leg.setFlip(looksRight(), sprite.leg.isFlipY)
        sprite.headAndBody.setFlip(looksRight(), sprite.leg.isFlipY)

        val backgroundTintColor = tintColor.cpy().sub(Color(0xAAAAAA shl 8))

        spriteBatch.drawSprite(sprite.leg, leftLegX, legY, -anim, tint = backgroundTintColor)
        spriteBatch.drawSprite(sprite.leg, rightLegX, legY, -anim, tint = backgroundTintColor)
        spriteBatch.drawSprite(sprite.headAndBody, x, y, tint = tintColor)
        spriteBatch.drawSprite(sprite.leg, leftLegX, legY, anim, tint = tintColor)
        spriteBatch.drawSprite(sprite.leg, rightLegX, legY, anim, tint = tintColor)
    }
    
    companion object {
        private const val WIDTH = 25f
        private const val HEIGHT = 18f
        private const val SPEED =  48f
        private const val JUMP_VELOCITY = -133.332f
        private const val MAX_HEALTH = 10
    }
}