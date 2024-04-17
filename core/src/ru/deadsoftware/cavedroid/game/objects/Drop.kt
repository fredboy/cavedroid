package ru.deadsoftware.cavedroid.game.objects

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Drop(
    x: Float,
    y: Float,
    val id: Int,
) : Rectangle(x, y, DROP_SIZE, DROP_SIZE) {

    val velocity = getInitialVelocity()
    var pickedUp = false

    fun canMagnetTo(rectangle: Rectangle): Boolean {
        val magnetArea = getMagnetArea()
        return Intersector.overlaps(magnetArea, rectangle)
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
        private const val MAGNET_DISTANCE = 16f

        const val MAGNET_VELOCITY = 128f
        const val DROP_SIZE = 8f

        private fun getInitialVelocity(): Vector2 = Vector2(0f, -1f)
    }
}
