package ru.fredboy.cavedroid.gameplay.physics.action.growblock

import com.badlogic.gdx.math.MathUtils

interface IGrowBlockAction {

    fun nextDelayTicks(): Long = MathUtils.random(30L, 180L)

    /**
     * @return true if the source block changed, false to reschedule it.
     *         A mature stem stays scheduled even when it produces fruit beside itself.
     */
    fun grow(x: Int, y: Int): Boolean
}
