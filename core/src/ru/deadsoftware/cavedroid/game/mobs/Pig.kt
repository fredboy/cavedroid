package ru.deadsoftware.cavedroid.game.mobs

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.item.InventoryItem
import ru.deadsoftware.cavedroid.misc.utils.drawSprite
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getBackgroundLeg
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getBody
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getForegroundLeg
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getLeftLegRelativeX
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getLegsRelativeY
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getRightLegRelativeX

class Pig(x: Float, y: Float) : PeacefulMob(x, y, WIDTH, HEIGHT, randomDir(), MAX_HEALTH) {

    override fun getSpeed(): Float {
        return SPEED
    }
    
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
            if (canJump()) {
                jump()
            }
        }
    }

    override fun getDrop(gameItemsHolder: GameItemsHolder): List<InventoryItem> {
        return listOf(gameItemsHolder.getItem("porkchop_raw").toInventoryItem())
    }

    override fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float) {
        updateAnimation(delta)

        val leftLegX = x + getLeftLegRelativeX(direction)
        val rightLegX = x + getRightLegRelativeX(direction)
        val legY = y + getLegsRelativeY()

        spriteBatch.drawSprite(getBackgroundLeg(), leftLegX, legY, -anim, tint = tintColor)
        spriteBatch.drawSprite(getBackgroundLeg(), rightLegX, legY, -anim, tint = tintColor)
        spriteBatch.drawSprite(getBody(direction), x, y, tint = tintColor)
        spriteBatch.drawSprite(getForegroundLeg(), leftLegX, legY, anim, tint = tintColor)
        spriteBatch.drawSprite(getForegroundLeg(), rightLegX, legY, anim, tint = tintColor)
    }
    
    
    private companion object {
        private const val WIDTH = 25f
        private const val HEIGHT = 18f
        private const val SPEED =  48f
        private const val JUMP_VELOCITY = -133.332f
        private const val MAX_HEALTH = 10
    }
}