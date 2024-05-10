package ru.deadsoftware.cavedroid.game.model.item

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.mobs.player.Inventory
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.drawSprite
import ru.deadsoftware.cavedroid.misc.utils.drawString
import ru.deadsoftware.cavedroid.misc.utils.px
import java.io.Serializable

class InventoryItem @JvmOverloads constructor(
    val itemKey: String,
    _amount: Int = 1,
) : Serializable {

    var amount = _amount
        set(value) {
            field = if (value < 0) {
                0
            } else {
                value
            }
        }

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

    @JvmOverloads
    fun add(count: Int = 1) {
        if (count > 0 && Int.MAX_VALUE - count < amount) {
            throw IllegalArgumentException("$amount + $count exceeds Int.MAX_VALUE")
        }

        amount += count
    }

    @JvmOverloads
    fun subtract(count: Int = 1) {
        if (count < 0) {
            throw IllegalArgumentException("Can't subtract negative amount")
        }

        add(-count)
    }

    @JvmOverloads
    fun canBeAdded(count: Int = 1): Boolean {
        return amount + count <= item.params.maxStack
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
        val amountString = amount.toString()
        spriteBatch.drawSprite(sprite, x - 10f, y - 10f, rotation = 0f, width = 20f, height = 20f)
        drawAmountText(
            spriteBatch = spriteBatch,
            text = amountString,
            x = x + 10f - Assets.getStringWidth(amountString) + 1f,
            y = y + 10f - Assets.getStringHeight(amountString) + 1f
        )
    }

    fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, x: Float, y: Float) {
        if (item.isNone()) {
            return
        }

        val sprite = item.sprite
        val placeableMarginTop = (item as? Item.Placeable)?.block?.params?.spriteMargins?.top ?: 0
        val placeableMarginLeft = (item as? Item.Placeable)?.block?.params?.spriteMargins?.left ?: 0
        spriteBatch.drawSprite(sprite, x + placeableMarginLeft, y + placeableMarginTop)

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

    companion object {
        fun InventoryItem?.isNoneOrNull() = this?.item == null || this.item.isNone()
    }
}
