package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.model.MobSprite
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class Cow(
    private val sprite: MobSprite.Cow,
) : AbstractPassiveMob(WIDTH, HEIGHT, MAX_HEALTH) {

    override val speed get() = SPEED

    override fun getDropItems(itemByKey: GetItemByKeyUseCase): List<InventoryItem> {
        return listOf(itemByKey["beef_raw"].toInventoryItem())
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float,
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
        private val WIDTH = 22.meters
        private val HEIGHT = 22.meters
        private const val SPEED = 3f
        private const val MAX_HEALTH = 10
    }
}
