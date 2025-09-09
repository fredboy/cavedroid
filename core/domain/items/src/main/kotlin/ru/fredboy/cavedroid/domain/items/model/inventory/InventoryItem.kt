package ru.fredboy.cavedroid.domain.items.model.inventory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ru.fredboy.cavedroid.common.utils.PIXELS_PER_METER
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.common.utils.drawString
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

    fun canBeAdded(count: Int = 1): Boolean = amount + count <= item.params.maxStack

    private fun drawAmountText(
        spriteBatch: SpriteBatch,
        font: BitmapFont,
        text: String,
        x: Float,
        y: Float,
    ) {
        spriteBatch.drawString(font, text, x + 1, y + 1, Color.BLACK)
        spriteBatch.drawString(font, text, x, y, Color.WHITE)
    }

    private fun drawAmountOrConditionBar(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        font: BitmapFont,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        getStringWidth: (String) -> Float,
        getStringHeight: (String) -> Float,
    ) {
        if (amount < 2) {
            return
        }

        if (item.isTool()) {
            spriteBatch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.GREEN
            shapeRenderer.rect(
                /* x = */ x,
                /* y = */ y + height - 2,
                /* width = */ width * (amount.toFloat() / item.params.maxStack.toFloat()),
                /* height = */ 2f,
            )
            shapeRenderer.end()
            spriteBatch.begin()
        } else {
            val amountString = amount.toString()
            drawAmountText(
                spriteBatch = spriteBatch,
                font = font,
                text = amountString,
                x = x + width - getStringWidth(amountString),
                y = y + height - getStringHeight(amountString),
            )
        }
    }

    fun draw(
        spriteBatch: SpriteBatch,
        shapeRenderer: ShapeRenderer,
        font: BitmapFont,
        x: Float,
        y: Float,
        getStringWidth: (String) -> Float,
        getStringHeight: (String) -> Float,
        width: Float? = null,
        height: Float? = null,
    ) {
        if (item.isNone()) {
            return
        }

        val sprite = item.sprite

        val drawWidth = width?.let { (it / PIXELS_PER_METER) * sprite.regionWidth.toFloat() }
            ?: sprite.regionWidth.toFloat()
        val drawHeight = height?.let { (it / PIXELS_PER_METER) * sprite.regionHeight.toFloat() }
            ?: sprite.regionHeight.toFloat()

        val placeableMarginTop = (item as? Item.Placeable)?.block?.params?.spriteMargins?.top ?: 0
        val placeableMarginLeft = (item as? Item.Placeable)?.block?.params?.spriteMargins?.left ?: 0
        spriteBatch.drawSprite(
            sprite = sprite,
            x = x + placeableMarginLeft,
            y = y + placeableMarginTop,
            width = drawWidth,
            height = drawHeight,
        )

        drawAmountOrConditionBar(
            spriteBatch = spriteBatch,
            shapeRenderer = shapeRenderer,
            font = font,
            x = x + placeableMarginLeft,
            y = y + placeableMarginTop,
            width = drawWidth,
            height = drawHeight,
            getStringWidth = getStringWidth,
            getStringHeight = getStringHeight,
        )
    }

    companion object {
        @OptIn(ExperimentalContracts::class)
        fun InventoryItem?.isNoneOrNull(): Boolean {
            contract { returns(false) implies (this@isNoneOrNull != null) }
            return this?.item == null || this.item.isNone()
        }
    }
}
