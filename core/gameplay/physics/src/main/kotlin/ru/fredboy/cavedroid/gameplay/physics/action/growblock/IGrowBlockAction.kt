package ru.fredboy.cavedroid.gameplay.physics.action.growblock

interface IGrowBlockAction {

    /**
     * @return true if the block grew, false if preconditions were not met
     *         (the queue will reschedule for another attempt).
     */
    fun grow(x: Int, y: Int): Boolean
}
