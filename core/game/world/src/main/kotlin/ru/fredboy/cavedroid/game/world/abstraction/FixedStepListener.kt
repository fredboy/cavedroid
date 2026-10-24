package ru.fredboy.cavedroid.game.world.abstraction

interface FixedStepListener {
    fun beforeStreaming() = Unit
    fun dispose() = Unit
    fun beforeStep(delta: Float)
    fun afterStep(delta: Float)
}
