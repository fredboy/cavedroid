package ru.fredboy.cavedroid.domain.items.model.inventory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.drawString
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.item.Item
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class InventoryItem(
    val item: Item,
    _amount: Int = 1,
) {

    var amount = _amount
        set(value) {
            field = if (value < 0) {
                0
            } else {
                value
            }
        }

    fun add(count: Int = 1) {
        if (count > 0 && Int.MAX_VALUE - count < amount) {
            throw IllegalArgumentException("$amount + $count exceeds Int.MAX_VALUE")
        }

        amount += count
    }

    fun subtract(count: Int = 1) {
        if (count < 0) {
            throw IllegalArgumentException("Can't subtract negative amount")
        }

        add(-count)
    }

    fun canBeAdded(count: Int = 1): Boolean {
        return amount + count <= item.params.maxStack
    }

    private fun drawAmountText(
        spriteBatch: SpriteBatch,
        font: BitmapFont,
        text: String,
        x: Float,
        y: Float
    ) {
        spriteBatch.drawString(font, text, x + 1, y + 1, Color.BLACK)
        spriteBatch.drawString(font, text, x, y, Color.WHITE)
    }

    fun drawSelected(
        spriteBatch: SpriteBatch,
        font: BitmapFont,
        x: Float,
        y: Float,
        getStringWidth: (String) -> Float,
        getStringHeight: (String) -> Float,
    ) {
        if (item.isNone()) {
            return
        }

        val sprite = item.sprite
        val amountString = amount.toString()
        spriteBatch.drawSprite(sprite, x - 10f, y - 10f, rotation = 0f, width = 20f, height = 20f)
        drawAmountText(
            spriteBatch = spriteBatch,
            font = font,
            text = amountString,
            x = x + 10f - getStringWidth(amountString) + 1f,
            y = y + 10f - getStringHeight(amountString) + 1f
        )
    }

    fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        font: BitmapFont,
        x: Float,
        y: Float,
        getStringWidth: (String) -> Float,
        getStringHeight: (String) -> Float,
    ) {
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
                font = font,
                text = amountString,
                x = x + 1.px - getStringWidth(amountString),
                y = y + 1.px - getStringHeight(amountString)
            )
        }
    }

    companion object {
        @OptIn(ExperimentalContracts::class)
        fun InventoryItem?.isNoneOrNull(): Boolean {
            contract { returns(false) implies(this@isNoneOrNull != null) }
            return this?.item == null || this.item.isNone()
        }
    }

}
