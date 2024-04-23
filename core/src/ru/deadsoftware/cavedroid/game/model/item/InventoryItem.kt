package ru.deadsoftware.cavedroid.game.model.item

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.drawSprite
import ru.deadsoftware.cavedroid.misc.utils.drawString
import ru.deadsoftware.cavedroid.misc.utils.px
import java.io.Serializable

class InventoryItem @JvmOverloads constructor(
    val itemKey: String,
    var amount: Int = 1,
) : Serializable {

    @Transient
    lateinit var item: Item
        private set

    @JvmOverloads
    constructor(_item: Item, amount: Int = 1) : this(_item.params.key, amount) {
        item = _item
    }

    fun init(gameItemsHolder: GameItemsHolder) {
        if (this::item.isInitialized) {
            return
        }
        item = gameItemsHolder.getItem(itemKey)
    }

    private fun drawAmountText(spriteBatch: SpriteBatch, text: String,  x: Float, y: Float) {
        spriteBatch.drawString(text, x + 1, y + 1, Color.BLACK)
        spriteBatch.drawString(text, x, y, Color.WHITE)
    }

    fun drawSelected(spriteBatch: SpriteBatch, x: Float, y: Float) {
        if (item.isNone()) {
            return
        }

        val sprite = item.sprite
        sprite.setOriginCenter()
        sprite.setPosition(x, y)
        sprite.setScale(1.25f)
        sprite.draw(spriteBatch)
        sprite.setScale(1f)
    }

    fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, x: Float, y: Float) {
        if (item.isNone()) {
            return
        }

        val sprite = item.sprite
        spriteBatch.drawSprite(sprite, x, y)

        if (amount < 2) {
            return
        }

        if (item.isTool()) {
            spriteBatch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.GREEN
            shapeRenderer.rect(
                /* x = */ x,
                /* y = */ y + 1.px - 2,
                /* width = */ 1.px * (amount.toFloat() / item.params.maxStack.toFloat()),
                /* height = */ 2f
            )
            shapeRenderer.end()
            spriteBatch.begin()
        } else {
            val amountString = amount.toString()
            drawAmountText(
                spriteBatch = spriteBatch,
                text = amountString,
                x = x + 1.px - Assets.getStringWidth(amountString),
                y = y + 1.px - Assets.getStringHeight(amountString)
            )
        }
    }

}
