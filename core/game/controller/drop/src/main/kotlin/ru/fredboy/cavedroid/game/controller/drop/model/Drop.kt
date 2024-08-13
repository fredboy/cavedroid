package ru.fredboy.cavedroid.game.controller.drop.model

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.utils.BLOCK_SIZE_PX
import ru.fredboy.cavedroid.domain.items.model.item.Item

class Drop(
    x: Float,
    y: Float,
    val item: Item,
    _amount: Int = 1,
) : Rectangle(x, y, DROP_SIZE, DROP_SIZE) {

    val velocity = getInitialVelocity()

    var isPickedUp = false
    var amount = _amount
        private set

    fun canMagnetTo(rectangle: Rectangle): Boolean {
        val magnetArea = getMagnetArea()
        return Intersector.overlaps(magnetArea, rectangle)
    }

    fun subtract(count: Int = 1) {
        if (count < 0) {
            throw IllegalArgumentException("Can't subtract negative amount")
        }

        amount -= count

        if (amount <= 0) {
            isPickedUp = true
        }
    }

    private fun getMagnetArea(): Rectangle {
        return Rectangle(
            /* x = */ x - MAGNET_DISTANCE,
            /* y = */ y - MAGNET_DISTANCE,
            /* width = */ width + MAGNET_DISTANCE * 2,
            /* height = */ height + MAGNET_DISTANCE * 2,
        )
    }


    companion object {
        private fun getInitialVelocity(): Vector2 = Vector2(0f, -1f)

        private const val MAGNET_DISTANCE = 8f

        const val MAGNET_VELOCITY = 256f
        const val DROP_SIZE = BLOCK_SIZE_PX / 2
    }

}