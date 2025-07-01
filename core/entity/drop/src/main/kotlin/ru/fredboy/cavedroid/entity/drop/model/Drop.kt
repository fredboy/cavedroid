package ru.fredboy.cavedroid.entity.drop.model

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.utils.BLOCK_SIZE_PX
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.item.Item

class Drop(
    x: Float,
    y: Float,
    val inventoryItem: InventoryItem,
) : Rectangle(x, y, DROP_SIZE, DROP_SIZE) {

    constructor(x: Float, y: Float, item: Item, amount: Int = 1) : this(x, y, InventoryItem(item, amount))

    val velocity = getInitialVelocity()

    var isPickedUp = false

    val item get() = inventoryItem.item

    val amount get() = inventoryItem.amount

    fun canMagnetTo(rectangle: Rectangle): Boolean {
        val magnetArea = getMagnetArea()
        return Intersector.overlaps(magnetArea, rectangle)
    }

    private fun getMagnetArea(): Rectangle = Rectangle(
        /* x = */ x - MAGNET_DISTANCE,
        /* y = */ y - MAGNET_DISTANCE,
        /* width = */ width + MAGNET_DISTANCE * 2,
        /* height = */ height + MAGNET_DISTANCE * 2,
    )

    companion object {
        private fun getInitialVelocity(): Vector2 = Vector2(MathUtils.random(-100f, 100f), -100f)

        private const val MAGNET_DISTANCE = 8f

        const val MAGNET_VELOCITY = 256f
        const val DROP_SIZE = BLOCK_SIZE_PX / 2
    }
}
