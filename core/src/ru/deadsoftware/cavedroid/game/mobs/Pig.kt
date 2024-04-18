package ru.deadsoftware.cavedroid.game.mobs

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.utils.drawSprite
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getBackgroundLeg
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getBody
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getForegroundLeg
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getLeftLegRelativeX
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getLegsRelativeY
import ru.deadsoftware.cavedroid.misc.utils.mobs.MobSprites.Pig.getRightLegRelativeX

class Pig(x: Float, y: Float) : Mob(x, y, WIDTH, HEIGHT, randomDir(), Type.MOB) {

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
    
    override fun ai(world: GameWorld, delta: Float) {
        if (MathUtils.randomBoolean(delta)) {
            if (velocity.x != 0f) {
                velocity.x = 0f
            } else {
                changeDir()
            }
        }
    }

    override fun draw(spriteBatch: SpriteBatch, x: Float, y: Float, delta: Float) {
        updateAnimation(delta)

        val leftLegX = x + getLeftLegRelativeX(direction)
        val rightLegX = x + getRightLegRelativeX(direction)
        val legY = y + getLegsRelativeY()

        spriteBatch.drawSprite(getBackgroundLeg(), leftLegX, legY, -anim)
        spriteBatch.drawSprite(getBackgroundLeg(), rightLegX, legY, -anim)
        spriteBatch.drawSprite(getBody(direction), x, y)
        spriteBatch.drawSprite(getForegroundLeg(), leftLegX, legY, anim)
        spriteBatch.drawSprite(getForegroundLeg(), rightLegX, legY, anim)
    }
    
    
    private companion object {
        private const val WIDTH = 25f
        private const val HEIGHT = 18f
        private const val SPEED =  69.072f
        private const val JUMP_VELOCITY = -133.332f
    }
}