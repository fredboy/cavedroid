package ru.deadsoftware.cavedroid.game.input

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils

class Joystick(
    private val value: Float,
) {

    var active = false
        private set
    var centerX = 0f
        private set
    var centerY = 0f
        private set

    var activeX = 0f
        private set
    var activeY = 0f
        private set

    var pointer = 0
        private set

    private val stickVector = Vector2()

    private var activateTimeMs = 0L

    fun activate(touchX: Float, touchY: Float, pointer: Int) {
        active = true
        centerX = touchX
        centerY = touchY
        activateTimeMs = TimeUtils.millis()
        this.pointer = pointer
    }

    fun deactivate() {
        active = false
    }

    fun getVelocityVector(): Vector2 {
        if (!active) {
            return Vector2.Zero
        }
        println(stickVector)
        return Vector2(
            stickVector.x * value,
            stickVector.y * value
        )
    }

    fun updateState(touchX: Float, touchY: Float) {
        if (!active) {
            return
        }

        stickVector.x = touchX - centerX
        stickVector.y = touchY - centerY
        stickVector.clamp(0f, RADIUS)

        activeX = centerX + stickVector.x
        activeY = centerY + stickVector.y

        stickVector.x /= RADIUS
        stickVector.y /= RADIUS
    }

    companion object {
        const val RADIUS = 24f
        const val SIZE = RADIUS * 2
        const val STICK_SIZE = 16f
    }

}