package ru.fredboy.cavedroid.domain.world.lighting

interface LightHandle {
    var isActive: Boolean
    fun setPosition(x: Float, y: Float)
    fun update()
    fun dispose()
}
